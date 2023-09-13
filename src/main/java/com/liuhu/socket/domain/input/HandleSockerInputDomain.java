package com.liuhu.socket.domain.input;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class HandleSockerInputDomain {

   private List<HandleSockerInnerInputDomain> socketList;

}
