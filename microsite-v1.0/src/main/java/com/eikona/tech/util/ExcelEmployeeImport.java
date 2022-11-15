package com.eikona.tech.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.DefaultConstants;
import com.eikona.tech.constants.MessageConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.entity.Department;
import com.eikona.tech.entity.Designation;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.Organization;
import com.eikona.tech.repository.DepartmentRepository;
import com.eikona.tech.repository.DesignationRepository;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.OrganizationRepository;

@Component
public class ExcelEmployeeImport {
	
	@Autowired
	private DepartmentRepository departmentrepository;
	
	@Autowired
	private DesignationRepository designationrepository;

	@Autowired
	private OrganizationRepository organizationrepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private EmployeeObjectMap employeeObjectMap;
	
	
	
	public Employee excelRowToEmployee(Row currentRow,Organization org, Map<String, Designation> designationMap,Map<String, Department> deptMap) {
		
		Employee employeeObj = null;
		
		Iterator<Cell> cellsInRow = currentRow.iterator();
		int cellIndex = NumberConstants.ZERO;
		employeeObj = new Employee();

		

		while (cellsInRow.hasNext()) {
			Cell currentCell = cellsInRow.next();

			if (null == employeeObj) {
				break;
			}

			else if (cellIndex == NumberConstants.ZERO) {
				setEmpId(employeeObj, currentCell);
			} else if (cellIndex == NumberConstants.ONE) {
				employeeObj.setName(currentCell.getStringCellValue());
			}
			
			else if (cellIndex == NumberConstants.TWO) {
				setDepartment(org, deptMap, employeeObj, currentCell);
			}

			else if (cellIndex == NumberConstants.THREE) {

				setDesignation(org, designationMap, employeeObj, currentCell);
			}

			else if (cellIndex == NumberConstants.FOUR) {
				setMobile(employeeObj, currentCell);
			}

			cellIndex++;
		}
		return employeeObj;
		
	}
	
	@SuppressWarnings(ApplicationConstants.DEPRECATION)
	private void setMobile(Employee employeeObj, Cell currentCell) {
		currentCell.setCellType(CellType.STRING);
		if (currentCell.getCellType() == CellType.NUMERIC) {
			employeeObj.setMobile(String.valueOf(currentCell.getNumericCellValue()));
		} else if (currentCell.getCellType() == CellType.STRING) {
			employeeObj.setMobile(currentCell.getStringCellValue());
		}
	}

	private void setDesignation(Organization org, Map<String, Designation> designationMap, Employee employeeObj,
			Cell currentCell) {
		String str = currentCell.getStringCellValue();
		if (null != str && !str.isEmpty()) {
			
			Designation designation = designationMap.get(str);
			if (null == designation) {
				designation = new Designation();
				designation.setName(str);
				designation.setOrganization(org);
				designationrepository.save(designation);
				designationMap.put(designation.getName(), designation);

			}
			employeeObj.setDesignation(designation);
		}
	}

	private void setDepartment(Organization org, Map<String, Department> deptMap, Employee employeeObj,
			Cell currentCell) {
		String str = currentCell.getStringCellValue();

		if (null != str && !str.isEmpty()) {
			
			Department department = deptMap.get(str);
			if (null == department) {
				department = new Department();
				department.setName(str);
				department.setOrganization(org);
				departmentrepository.save(department);
				deptMap.put(department.getName(), department);
			}
			employeeObj.setDepartment(department);
		}
	}


	@SuppressWarnings(ApplicationConstants.DEPRECATION)
	private void setEmpId(Employee employeeObj, Cell currentCell) {
		currentCell.setCellType(CellType.STRING);
		if (currentCell.getCellType() == CellType.NUMERIC) {
			employeeObj.setEmpId(String.valueOf(currentCell.getNumericCellValue()));
		} else if (currentCell.getCellType() == CellType.STRING) {
			employeeObj.setEmpId(currentCell.getStringCellValue());
		}
	}
	public List<Employee> parseExcelFileEmployeeList(InputStream inputStream) {
		List<Employee> employeeList = new ArrayList<Employee>();
		try {

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(NumberConstants.ZERO);

			Iterator<Row> rows = sheet.iterator();

			

			int rowNumber = NumberConstants.ZERO;
			Map<String, Designation> designationMap = employeeObjectMap.getDesignation();
			Map<String, Department> deptMap = employeeObjectMap.getDepartment();
			List<String> empIdList=employeeRepository.getEmpIdAndIsDeletedFalseCustom();
			Organization org = organizationrepository.findById(DefaultConstants.DEFAULT_ORGANIZATION_ID).get();
			while (rows.hasNext()) {
				Row currentRow = rows.next();

				// skip header
				if (rowNumber == NumberConstants.ZERO) {
					rowNumber++;
					continue;
				}

				rowNumber++;
				
				Employee employee=excelRowToEmployee(currentRow,org,designationMap,deptMap);
				
				boolean isContains=empIdList.contains(employee.getEmpId());
				
				if(!isContains && null!=employee.getName() && !employee.getName().isEmpty() && null!=employee.getEmpId()&& !employee.getEmpId().isEmpty())
				employeeList.add(employee);
				
				if(rowNumber%NumberConstants.HUNDRED==NumberConstants.ZERO) {
					employeeRepository.saveAll(employeeList);
					employeeList.clear();
				}
					
					
			}
			
			if(!employeeList.isEmpty()) {
				employeeRepository.saveAll(employeeList);
				employeeList.clear();
			}
			
			workbook.close();

			return employeeList;
		} catch (IOException e) {
			throw new RuntimeException(MessageConstants.FAILED_MESSAGE + e.getMessage());
		}
	}
	
	
public Employee cosecExcelRowToEmployee(Row currentRow,Organization org) {
		
		Employee employeeObj = null;
		
		Iterator<Cell> cellsInRow = currentRow.iterator();
		int cellIndex = NumberConstants.ZERO;
		employeeObj = new Employee();
		while (cellsInRow.hasNext()) {
			Cell currentCell = cellsInRow.next();

			if (null == employeeObj) {
				break;
			}

			else if (cellIndex == NumberConstants.ZERO) {
				setEmpId(employeeObj, currentCell);
			} else if (cellIndex == NumberConstants.TWO) {
				employeeObj.setName(currentCell.getStringCellValue());
				employeeObj.setOrganization(org);
			}

			cellIndex++;
		}
		return employeeObj;
		
	}
	public List<Employee> parseCosecExcelFileEmployeeList(InputStream inputStream) throws InvalidFormatException {
		List<Employee> employeeList = new ArrayList<Employee>();
		try {
		//OPCPackage pkg = OPCPackage.open(inputStream);

			Workbook workbook = new HSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(NumberConstants.ZERO);

			Iterator<Row> rows = sheet.iterator();

			

			int rowNumber = NumberConstants.ZERO;
			List<String> empIdList=employeeRepository.getEmpIdAndIsDeletedFalseCustom();
			Organization org = organizationrepository.findById(DefaultConstants.DEFAULT_ORGANIZATION_ID).get();
			while (rows.hasNext()) {
				Row currentRow = rows.next();

				// skip header
				if (rowNumber == NumberConstants.ZERO) {
					rowNumber++;
					continue;
				}

				rowNumber++;
				
				Employee employee=cosecExcelRowToEmployee(currentRow,org);
				
				boolean isContains=empIdList.contains(employee.getEmpId());
				
				if(!isContains && null!=employee.getName() && !employee.getName().isEmpty() && null!=employee.getEmpId()&& !employee.getEmpId().isEmpty())
				employeeList.add(employee);
				
				if(rowNumber%NumberConstants.HUNDRED==NumberConstants.ZERO) {
					employeeRepository.saveAll(employeeList);
					employeeList.clear();
				}
					
					
			}
			
			if(!employeeList.isEmpty()) {
				employeeRepository.saveAll(employeeList);
				employeeList.clear();
			}
			
			workbook.close();

			return employeeList;
		} catch (IOException e) {
			throw new RuntimeException(MessageConstants.FAILED_MESSAGE + e.getMessage());
		}
	}

	
	
}
