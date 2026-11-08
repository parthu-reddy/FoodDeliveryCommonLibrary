package com.fooddelivery.common.exception;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A client that hangs up mid-response is not a server fault. Seen on Dev 2026-11-08T07:08:21Z on
 * restaurant-service: {@code ByteArrayHttpMessageConverter} hit "Broken pipe", Spring raised
 * {@code AsyncRequestNotUsableException}, and the catch-all logged it as ERROR "Unhandled exception" and then
 * tried to write a 500 body down the dead connection. The handler must write nothing and log one WARN line
 * naming the request -- while a downstream connection reset, which reads the same, stays a 500 at ERROR.
 */
class GlobalExceptionHandlerClientDisconnectTest {

    @RestController
    static class Endpoints {
        @GetMapping("/async-not-usable")
        String asyncNotUsable() throws AsyncRequestNotUsableException {
            throw new AsyncRequestNotUsableException(
                    "ServletOutputStream failed to write: java.io.IOException: Broken pipe");
        }

        /** Real path: the converter writes the bytes and the container's stream reports the abort. */
        @GetMapping("/bytes")
        byte[] bytes() {
            return new byte[] {1, 2, 3};
        }

        @GetMapping("/wrapped-abort")
        String wrappedAbort() {
            throw new HttpMessageNotWritableException("Could not write JSON",
                    new ClientAbortException(new IOException("Broken pipe")));
        }

        /** An outbound call whose peer reset: same words, but it is our failure, not the client's. */
        @GetMapping("/downstream-reset")
        String downstreamReset() throws IOException {
            throw new IOException("Connection reset by peer");
        }
    }

    /** Stands in for Tomcat once the client has gone: every write throws ClientAbortException. */
    private static final Filter BROKEN_PIPE_ON_BYTES = (request, response, chain) -> {
        String uri = ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI();
        if (!uri.equals("/bytes")) {
            chain.doFilter(request, response);
            return;
        }
        chain.doFilter(request, new HttpServletResponseWrapper((HttpServletResponse) response) {
            @Override
            public ServletOutputStream getOutputStream() {
                return new ServletOutputStream() {
                    @Override public boolean isReady() { return true; }
                    @Override public void setWriteListener(WriteListener listener) { }
                    @Override public void write(int b) throws IOException {
                        throw new ClientAbortException(new IOException("Broken pipe"));
                    }
                    @Override public void write(byte[] b, int off, int len) throws IOException {
                        throw new ClientAbortException(new IOException("Broken pipe"));
                    }
                };
            }
        });
    };

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new Endpoints())
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilters(BROKEN_PIPE_ON_BYTES)
            .build();

    private final Logger handlerLog = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ListAppender<ILoggingEvent> logged = new ListAppender<>();

    @BeforeEach
    void captureLog() {
        logged.start();
        handlerLog.addAppender(logged);
    }

    @AfterEach
    void releaseLog() {
        handlerLog.detachAppender(logged);
    }

    @Test
    void asyncRequestNotUsableWritesNothingAndLogsOneWarnWithThePath() throws Exception {
        MvcResult result = mockMvc.perform(get("/async-not-usable")).andReturn();
        assertNothingWrittenAndOneWarnFor(result, "GET /async-not-usable");
    }

    @Test
    void aClientAbortWhileTheConverterWritesBytesWritesNothingAndLogsOneWarnWithThePath() throws Exception {
        MvcResult result = mockMvc.perform(get("/bytes")).andReturn();
        assertNothingWrittenAndOneWarnFor(result, "GET /bytes");
    }

    @Test
    void aClientAbortWrappedByAConverterIsStillAClientDisconnect() throws Exception {
        MvcResult result = mockMvc.perform(get("/wrapped-abort")).andReturn();
        assertNothingWrittenAndOneWarnFor(result, "GET /wrapped-abort");
    }

    @Test
    void aDownstreamConnectionResetIsStillA500LoggedAtError() throws Exception {
        mockMvc.perform(get("/downstream-reset")).andExpect(status().isInternalServerError());
        assertThat(levels()).containsExactly(Level.ERROR);
        assertThat(logged.list.get(0).getFormattedMessage()).contains("Unhandled exception");
    }

    private void assertNothingWrittenAndOneWarnFor(MvcResult result, String methodAndPath) throws Exception {
        assertThat(result.getResponse().getContentAsString()).isEmpty();
        assertThat(result.getResponse().getStatus()).isNotEqualTo(500);
        assertThat(levels()).containsExactly(Level.WARN);
        ILoggingEvent line = logged.list.get(0);
        assertThat(line.getFormattedMessage()).contains(methodAndPath);
        assertThat(line.getThrowableProxy()).as("one line, no stack trace").isNull();
    }

    private List<Level> levels() {
        return logged.list.stream().map(ILoggingEvent::getLevel).toList();
    }
}
