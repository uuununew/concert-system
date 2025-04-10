package kr.hhplus.be.server.presentation.cash;

import jakarta.validation.Valid;
import kr.hhplus.be.server.application.cash.CashService;
import kr.hhplus.be.server.application.cash.ChargeCashCommand;
import kr.hhplus.be.server.application.cash.UseCashCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cash")
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    /**
     * [POST] /cash/charge
     * 유저의 캐시에 금액을 충전하는 API
     *
     * @param request 충전 요청 DTO (userId, amount 포함)
     * @return 충전 후 잔액을 반환하는 CashResponse DTO
     */
    @PostMapping("/charge")
    public CashResponse charge(@RequestBody @Valid ChargeCashRequest request) {
        ChargeCashCommand command = new ChargeCashCommand(request.userId(), request.amount());
        return cashService.charge(command);
    }

    /**
     * [POST] /cash/use
     * 유저의 캐시를 사용하는 API
     *
     * @param request 사용 요청 DTO (userId, amount 포함)
     * @return 사용 후 잔액을 반환하는 CashResponse DTO
     */
    @PostMapping("/use")
    public CashResponse use(@RequestBody @Valid UseCashRequest request) {
        UseCashCommand command = new UseCashCommand(request.userId(), request.amount());
        return cashService.use(command);
    }

    /**
     * [GET] /cash/history
     * 유저의 캐시 충전/사용 이력을 조회하는 API
     *
     * @param userId 이력을 조회할 유저 ID
     * @return 캐시 이력 리스트를 반환하는 List<CashHistoryResponse>
     */
    @GetMapping("/history")
    public List<CashHistoryResponse> getHistory(@RequestParam Long userId) {
        return cashService.getHistories(userId).stream()
                .map(CashHistoryResponse::from)
                .toList();
    }
}
