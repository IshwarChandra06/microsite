package com.eikona.tech.service.impl.model;



import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.constants.OrganizationConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Organization;
import com.eikona.tech.repository.OrganizationRepository;
import com.eikona.tech.service.OrganizationService;
import com.eikona.tech.util.GeneralSpecificationUtil;


@Service
public class OrganizationServiceImpl implements OrganizationService {
	
	@Autowired
    private OrganizationRepository organizationRepository;
	
	@Autowired
    private GeneralSpecificationUtil<Organization> generalSpecificationOrg;

	@Override
	public List<Organization> getAll() {
		 return organizationRepository.findAllByIsDeletedFalse();
		
	}
	
	@Override
	public Organization save(Organization organization) {
		organization.setDeleted(false);
        return this.organizationRepository.save(organization);
    	
    }
	
	@Override
	public  Organization getById(Long id) {
		Optional<Organization> optional = organizationRepository.findById(id);
		Organization organization = null;
        if (optional.isPresent()) {
        	organization = optional.get();
        } else {
            throw new RuntimeException(OrganizationConstants.ORGANIZATION_NOT_FOUND + id);
        }
        return organization;
    	
    }
	
	@Override
	public void deleteById(long id) {
		Optional<Organization> optional = organizationRepository.findById(id);
		Organization department = null;
        if (optional.isPresent()) {
        	department = optional.get();
        	department.setDeleted(true);
        } else {
            throw new RuntimeException(OrganizationConstants.ORGANIZATION_NOT_FOUND + id);
        }
        this.organizationRepository.save(department);
	}

	

	@Override
	public PaginationDto<Organization> searchByField(Long id, String name, String address, String city, String pin,
			int pageno, String sortField, String sortDir, Organization organization) {
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<Organization> page = getOrganizationPage(id, name, address, city, pin, pageno, sortField, sortDir, organization);
        List<Organization> organizationList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<Organization> dtoList = new PaginationDto<Organization>(organizationList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<Organization> getOrganizationPage(Long id, String name, String address, String city, String pin,
			int pageno, String sortField, String sortDir, Organization organization) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		
		Specification<Organization> idSpc = generalSpecificationOrg.longSpecification(id, ApplicationConstants.ID);
		Specification<Organization> nameSpc = generalSpecificationOrg.stringSpecification(name, ApplicationConstants.NAME);
		Specification<Organization> addressSpc = generalSpecificationOrg.stringSpecification(address, OrganizationConstants.ADDRESS);
		Specification<Organization> citySpc = generalSpecificationOrg.stringSpecification(city, OrganizationConstants.CITY);
		Specification<Organization> pinSpc = generalSpecificationOrg.stringSpecification(pin, OrganizationConstants.PINCODE);
		Specification<Organization> isDeletedFalse = generalSpecificationOrg.isDeletedSpecification();
		
		String orgName = (null == organization)? null: organization.getName();
		Specification<Organization> orgSpc = generalSpecificationOrg.stringSpecification(orgName, ApplicationConstants.NAME);
		
    	Page<Organization> page = organizationRepository.findAll(idSpc.and(nameSpc).and(isDeletedFalse).and(addressSpc).and(citySpc).and(pinSpc).and(orgSpc), pageable);
		return page;
	}
}
