package com.liuhu.socket.algorithm.service;

import com.liuhu.socket.domain.input.MarketInput2Domain;

import java.util.List;

public interface HandleService {

    List randomSelectedSocket(MarketInput2Domain input2Domain);
}
