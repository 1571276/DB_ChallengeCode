package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransferDto {

	private String accountFrom;
	private String accountTo;
	private BigDecimal amount;
	
}
