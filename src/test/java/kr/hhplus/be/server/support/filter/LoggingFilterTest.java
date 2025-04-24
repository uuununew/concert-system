package kr.hhplus.be.server.support.filter;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
public class LoggingFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loggingFilter_logsRequestAndResponseBody(CapturedOutput output) throws Exception {
        // when
        mockMvc.perform(post("/token/123")
                        .header("X-USER-ID", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isOk());

        // then
        assertThat(output.getOut()).contains("Request");
        assertThat(output.getOut()).contains("Response");
    }
}
