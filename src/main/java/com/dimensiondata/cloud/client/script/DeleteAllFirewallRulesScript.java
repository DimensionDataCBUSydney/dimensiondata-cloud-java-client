package com.dimensiondata.cloud.client.script;

import com.dimensiondata.cloud.client.*;
import com.dimensiondata.cloud.client.http.CallableDeletedState;
import com.dimensiondata.cloud.client.model.FirewallRuleType;
import com.dimensiondata.cloud.client.model.FirewallRules;

import java.util.List;

import static com.dimensiondata.cloud.client.script.Script.*;

public class DeleteAllFirewallRulesScript implements NetworkDomainScript
{
    @Override
    public void execute(Cloud cloud, String networkDomainId)
    {
        Filter filter = new Filter(new Param(FirewallService.PARAMETER_NETWORKDOMAIN_ID, networkDomainId),
                new Param(FirewallService.PARAMETER_NAME + Filter.LIKE_SUFFIX, "CCDEFAULT*"));
        FirewallRules firewallRules = cloud.firewall().listFirewallRules(PAGE_SIZE, 1, OrderBy.EMPTY, filter);
        int defaultRulesCount = firewallRules.getTotalCount();

        filter = new Filter(new Param(ServerService.PARAMETER_NETWORKDOMAIN_ID, networkDomainId));
        firewallRules = cloud.firewall().listFirewallRules(PAGE_SIZE + defaultRulesCount, 1, OrderBy.EMPTY, filter);
        println("FirewallRules to delete: " + (firewallRules.getTotalCount() - defaultRulesCount));
        while (firewallRules.getTotalCount() > defaultRulesCount)
        {
            deleteFirewallRules(cloud, firewallRules.getFirewallRule());
            firewallRules = cloud.firewall().listFirewallRules(PAGE_SIZE + defaultRulesCount, 1, OrderBy.EMPTY, filter);
        }
    }

    private static void deleteFirewallRules(Cloud cloud, List<FirewallRuleType> firewallRules)
    {
        // check all rules are in a NORMAL state
        for (FirewallRuleType firewallRule : firewallRules)
        {
            if (!firewallRule.getState().equals(NORMAL_STATE))
            {
                throw new RuntimeException("FirewallRule not in NORMAL state: " + firewallRule.getId());
            }
        }

        for (FirewallRuleType firewallRule : firewallRules)
        {
            if (firewallRule.getRuleType().equals("CLIENT_RULE"))
            {
                cloud.firewall().deleteFirewallRule(firewallRule.getId());
                awaitUntil("Deleting FirewallRule " + firewallRule.getId(), new CallableDeletedState(cloud.firewall(), "firewallRule", firewallRule.getId()));
            }
        }
    }
}
