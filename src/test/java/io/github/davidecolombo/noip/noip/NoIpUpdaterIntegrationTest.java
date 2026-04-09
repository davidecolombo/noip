package io.github.davidecolombo.noip.noip;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.davidecolombo.noip.NoIpSettings;
import io.github.davidecolombo.noip.NoIpUpdater;
import io.github.davidecolombo.noip.exception.NoIpException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Retrofit;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;