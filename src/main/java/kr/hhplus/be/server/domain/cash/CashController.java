package kr.hhplus.be.server.domain.cash;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cash")
public class CashController {

    private final CashService cashService;

    @PostMapping("/charge")
    public ResponseEntity<CashResponse> charge(@RequestBody ChargeRequest request){
        BigDecimal chargedAmount = cashService.charge(request.getUserId(), request.getAmount());
        return ResponseEntity.ok(new CashResponse(chargedAmount));
    }

    @PostMapping("/use")
    public ResponseEntity<CashResponse> use(@RequestBody UseRequest request) {
        BigDecimal remaining = cashService.use(request.getUserId(), request.getAmount());
        return ResponseEntity.ok(new CashResponse(remaining));
    }
}
