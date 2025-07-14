package com.hanyang.api.dataOffer.controller;

import com.hanyang.api.core.response.ApiResponse;
import com.hanyang.api.dataOffer.domain.DataOffer;
import com.hanyang.api.dataOffer.dto.ReqDataOfferDto;
import com.hanyang.api.dataOffer.dto.ResDataOfferDto;
import com.hanyang.api.dataOffer.dto.ResDataOfferListDto;
import com.hanyang.api.dataOffer.service.DataOfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@Tag(name = "데이터제공신청 API")
@RestController
@RequestMapping("/api")
public class DataOfferController {
    private final DataOfferService dataOfferService;
    public DataOfferController(DataOfferService dataOfferService) {
        this.dataOfferService = dataOfferService;
    }

    @Operation(summary = "데이터제공 요청")
    @PostMapping("/dataOffer")
    public ResponseEntity<ApiResponse<ResDataOfferDto>> dataOffer(@AuthenticationPrincipal UserDetails userDetails,
                                                                  @RequestBody ReqDataOfferDto reqDataOfferDto ){

        DataOffer dataOffer = dataOfferService.create(reqDataOfferDto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(new ResDataOfferDto(dataOffer)));
    }

    @Operation(summary = "데이터제공 요청리스트")
    @GetMapping("/dataOffer/list")
    public ResponseEntity<ApiResponse<ResDataOfferListDto>> getDataOfferList(@AuthenticationPrincipal UserDetails userDetails,
                                                                         @RequestParam int page) {
        String userName = userDetails.getUsername();
        Page<DataOffer> dataOffers = dataOfferService.getDataOfferList(userName, page);
        return ResponseEntity.ok(ApiResponse.ok(new ResDataOfferListDto(dataOffers)));

    }

    @Operation(summary = "데이터 요청 상세조회")
    @GetMapping("/dataOffer/{dataOfferId}")
    public ResponseEntity<ApiResponse<ResDataOfferDto>> getDataOffer(@PathVariable Long dataOfferId){
        DataOffer dataOffer = dataOfferService.getDataOffer(dataOfferId);
        return ResponseEntity.ok(ApiResponse.ok(new ResDataOfferDto(dataOffer)));
    }


}
