package com.eikona.tech.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;

import com.eikona.tech.entity.Transaction;

public interface TransactionRepository extends DataTablesRepository<Transaction, Long>{

	@Query("SELECT tr FROM com.eikona.tech.entity.Transaction as tr where tr.punchDate >=:sDate and tr.punchDate <=:eDate and tr.organization =:organization"
			+ " and tr.employeeCode is not null order by tr.punchDateStr asc, tr.punchTimeStr asc")
	List<Transaction> getTransactionData(Date sDate, Date eDate, String organization);

	List<Transaction> findByPunchDateStrAndOrganization(String currDate, String currOrg);

	List<Transaction> findByPunchDateStrAndOrganizationIn(String currDate, List<String> orgList);

}
