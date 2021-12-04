package space.davidecolombo.noip.noip;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import retrofit2.Response;
import retrofit2.mock.Calls;
import space.davidecolombo.noip.TestUtils;

import java.io.IOException;

class INoIpApiTest {

    @Test
    void shouldUpdateNoIpAddress() throws IOException {

        String username = "username";
        String password = "password";
        String userAgent = "user-agent";
        String mockedResponse = "good " + TestUtils.LOOPBACK_ADDRESS;

        INoIpApi api = Mockito.spy(INoIpApi.build(username, password, userAgent));
        Mockito.when(api.update(
                Mockito.anyString()
                , Mockito.anyString()
        )).thenReturn(Calls.response(mockedResponse));

        Response<String> response = api
                .update(username, TestUtils.LOOPBACK_ADDRESS)
                .execute();

        Assertions.assertEquals(mockedResponse, response.body());
    }
}