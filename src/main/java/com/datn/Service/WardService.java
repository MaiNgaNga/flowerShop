package com.datn.Service;

import com.datn.model.Ward;

import java.util.List;

public interface WardService {
    List<Ward> getAllWards();
    Ward updateWard(Long id, Ward ward);
    Double getShipFeeByWardId(Long wardId) ;
}
