package com.CRM.Util;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Principal {
    private String authifyerId;
    private String email;
    private String firstName;
    private String lastName;
}
