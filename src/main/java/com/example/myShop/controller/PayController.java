package com.example.myShop.controller;

import com.example.myShop.dto.OrderDto;
import com.example.myShop.dto.PayApproveRequestDto;
import com.example.myShop.dto.PayRequestDto;
import com.example.myShop.entity.Order;
import com.example.myShop.repository.ItemRepository;
import com.example.myShop.service.KakaoPayService;
import com.example.myShop.service.OrderService;
import com.example.myShop.service.SlackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PayController {

    private final KakaoPayService kakaoPayService;
    private final OrderService orderService;
    private final SlackService slackService;
    private final ItemRepository itemRepostitory;

    @PostMapping(value = "/pay")//itemdetail.html의 order에서 url을 /pay로 바꾼거에요
    @ResponseBody
    public ResponseEntity<Map<String,String>> kakaoPay(@RequestBody PayRequestDto payRequestDto, HttpSession session) {
        // KakaoPayService redirect URL 받아서 응답
        String url = kakaoPayService.kakaoPayReady(payRequestDto, session);

        // 클라이언트에 redirect url을 json으로 응답(QR코드 응답 실행됨)
        Map<String, String> response = new HashMap<>();
        response.put("redirectUrl", url);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/kakaoPaySuccess")//위 QR코드에서 결제 성공시 호출
    @ResponseBody
    public String kakaoPaySuccess(@RequestParam String pg_token, HttpSession session, Principal principal, Model model) {
        try {
            String tid = (String) session.getAttribute("tid");
            Long itemId = (Long) session.getAttribute("itemId");
            Integer count = (Integer) session.getAttribute("count");

            //세션이 유효한가?
            if (tid == null || itemId == null || count == null) {
                model.addAttribute("msg", "세션 정보가 유효하지 않습니다.");
                return "redirect:/?fail";
            }



            //주문 생성 DTO
            PayApproveRequestDto approveDto = new PayApproveRequestDto();
            approveDto.setTid(tid);
            approveDto.setPgToken(pg_token);
            approveDto.setPartnerOrderId("order_mock_0001");
            approveDto.setPartnerUserId("user_mock");

            //결제 승인 요청
            String result = kakaoPayService.kakaoPayApprove(approveDto);

            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(itemId);
            orderDto.setCount(count);

            String itemName = itemRepostitory.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."))
                    .getItemName();


            String email = principal.getName();
            Long orderId = orderService.order(orderDto, email);

            //슬랙에 메세지 보냄
            slackService.sendMessage("[Team4]에서 알려드립니다.\n"  + email + "님의 주문이 완료되었습니다.\n 주문번호 : " + orderId + "\n 주문 상품 : " + itemName);

            //확인창 나오고 메인으로 이동
            return "<script>alert('결제가 완료되었습니다! 주문번호: " + orderId + "'); location.href='/'</script>";
        } catch (Exception e){
            model.addAttribute("msg", "결제 중 오류 발생" + e.getMessage());
            return "<script>alert('결제 중 오류 발생: " + e.getMessage() + "'); location.href='/'</script>";
        }


    }

    //아직
    @GetMapping(value = "/kakaoPayCancel")
    public String kakaoPayCancel(){
        return "redirect:/order/cancel";
    }

    @GetMapping(value ="/kakaoPayFail")
    public String kakaoPayFail(){
        return "redirect:/order/fail";
    }
}
