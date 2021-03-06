package com.dimensiondata.cloud.client.http;

import java.util.List;

import com.dimensiondata.cloud.client.model.NameValuePairType;

public class AbstractRestfulService
{
    protected final String baseUrl;

    protected final HttpClient httpClient;

    public AbstractRestfulService(String baseUrl)
    {
        this.baseUrl = baseUrl;
        this.httpClient = new HttpClient(baseUrl);
    }

    protected static void assertParameterEquals(String name, String expectedValue, String value)
    {
        if (!expectedValue.equals(value))
        {
            throw new InvalidParameterException("Expected " + name + ": " + expectedValue + " but was: " + value);
        }
    }

    public static NameValuePairType findRequiredNameValuePair(String name, List<NameValuePairType> nameValuePairs)
    {
        for (NameValuePairType nameValuePair : nameValuePairs)
        {
            if (name.equals(nameValuePair.getName()))
            {
                return nameValuePair;
            }
        }
        throw new InvalidParameterException(name + " not found in Response");
    }
}
