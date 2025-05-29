package kr.hhplus.be.server.infrastructure.test.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kafka")
public class KafkaTestController {
    private final MessageProducer producer;

    @PostMapping("/send")
    public String sendMessage(@RequestParam String message) {
        producer.send("test-topic", message);
        return "✅ 메시지 전송 완료: " + message;
    }
}
