package io.github.davidecolombo.noip.retrofit;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

@RequiredArgsConstructor
public class UserAgentInterceptor implements Interceptor {

	@NonNull
	private final String userAgent;

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request();
		Request requestWithUserAgent = request.newBuilder().header("User-Agent", userAgent).build();
		return chain.proceed(requestWithUserAgent);
	}
}