package io.github.davidecolombo.noip.noip;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface for No-IP API using Retrofit2.
 * Provides methods to update DNS records for dynamic IP addresses.
 */
public interface INoIpApi {

    /**
     * Update No-IP DNS record with the specified IP address.
     *
     * @param hostname the hostname to update
     * @param ip the IP address to set
     * @return Retrofit Call with the response
     */
    @GET("/nic/update")
    Call<String> update(
            @Query("hostname") String hostname,
            @Query("ip") String ip
    );
}