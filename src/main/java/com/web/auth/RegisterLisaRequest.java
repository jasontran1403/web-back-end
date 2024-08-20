package com.web.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterLisaRequest {

  private String firstname;
  private String lastname;
  private String email;
  private String password;
  private String code;
  private String refferal;
  private String branchName;
}
