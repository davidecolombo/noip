package io.github.davidecolombo.noip.noip;

import io.github.davidecolombo.noip.NoIpSettings;
import io.github.davidecolombo.noip.NoIpUpdater;
import io.github.davidecolombo.noip.TestUtils;
import io.github.davidecolombo.noip.exception.NoIpException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import retrofit2.mock.Calls;

import java.io.IOException;