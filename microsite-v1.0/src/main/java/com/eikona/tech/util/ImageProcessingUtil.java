package com.eikona.tech.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.DefaultConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.Image;
import com.eikona.tech.entity.Transaction;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.ImageRepository;

import net.coobird.thumbnailator.Thumbnails;

@Component
public class ImageProcessingUtil {

	@Autowired
	private EmployeeRepository employeeRepository;
	@Autowired
	private ImageRepository imageRepository;

	public Employee searchEmployee(String fileName) {
		String[] imageNameArray = fileName.split(ApplicationConstants.DELIMITER_UNDERSCORE);

		String secondPart = imageNameArray[NumberConstants.ONE];
		String[] splitBydot = secondPart.split(Pattern.quote(ApplicationConstants.DELIMITER_DOT));
		String empId = splitBydot[NumberConstants.ZERO];
		return employeeRepository.findByEmpIdAndIsDeletedFalse(empId);
	}

	@SuppressWarnings(ApplicationConstants.UNUSED)
	public byte[] searchEmployeeImage(long id) {
		Employee emp = new Employee();
		emp.setId(id);
		List<Image> imageList = imageRepository.findByEmployee(emp);
		String bytesBase64 = null;
		byte[] bytes = null;
		if (null != imageList) {

			try {
				for (Image image : imageList) {
					String imagePath = image.getThumbnailPath();
					InputStream inputStream = new FileInputStream(imagePath);
					bytes = IOUtils.toByteArray(inputStream);
					bytesBase64 = Base64.encodeBase64String(bytes);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bytes;
	}

	public byte[] searchTransactionImage(Transaction trans) {

		byte[] bytes = null;
		try {
			if(null != trans.getCropimagePath()) {
				String imagePath = trans.getCropimagePath();
				InputStream inputStream = new FileInputStream(imagePath);

				bytes = IOUtils.toByteArray(inputStream);
				Base64.encodeBase64String(bytes);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bytes;
	}

	public String[] imageProcessing(BufferedImage originalImage, String fileName) {
		String[] stringArray = new String[NumberConstants.THREE];
		try {
			String writeImageForOrginal = ApplicationConstants.DELIMITER_EMPTY;
			String writeImageForThumbnail = ApplicationConstants.DELIMITER_EMPTY;
			String writeImageForResize = ApplicationConstants.DELIMITER_EMPTY;

			String[] imageNameArray = fileName.split(ApplicationConstants.DELIMITER_UNDERSCORE);

			String secondPart = imageNameArray[NumberConstants.ONE];
			String[] splitBydot = secondPart.split(Pattern.quote(ApplicationConstants.DELIMITER_DOT));
			String empId = splitBydot[NumberConstants.ZERO];

			File original = new File(DefaultConstants.EMPLOYEE_ORIGINAL_PATH);
			writeImageForOrginal = getOriginalImagePath(stringArray, imageNameArray, empId, original);

			File resize = new File(DefaultConstants.EMPLOYEE_RESIZE_PATH);
			writeImageForResize = getResizeImagePath(stringArray, imageNameArray, empId, resize);

			File thumbnail = new File(DefaultConstants.EMPLOYEE_THUIMBNAIL_PATH);
			writeImageForThumbnail = getThumbnailImagePath(stringArray, imageNameArray, empId, thumbnail);

			if (null != originalImage) {
				ImageIO.write(originalImage, ApplicationConstants.FORMAT_JPG, new File(writeImageForOrginal));

				BufferedImage outputImageForThumbnail = resizeImage(originalImage, NumberConstants.HUNDRED,
						NumberConstants.HUNDRED);
				BufferedImage outputImageForResize = resizeImage(originalImage, NumberConstants.THREE_HUNDRED,
						NumberConstants.THREE_HUNDRED);

				ImageIO.write(outputImageForThumbnail, ApplicationConstants.FORMAT_JPG,
						new File(writeImageForThumbnail));
				ImageIO.write(outputImageForResize, ApplicationConstants.FORMAT_JPG, new File(writeImageForResize));
			}
			return stringArray;
		} catch (IOException e) {
			e.printStackTrace();

			return stringArray;
		}

	}

	private String getThumbnailImagePath(String[] stringArray, String[] imageNameArray, String empId, File thum) {
		String writeImageForEmployee;
		String thumDirPath = DefaultConstants.EMPLOYEE_THUIMBNAIL_PATH;
		if (!thum.exists()) {
			thum.mkdirs();
			writeImageForEmployee = stringArray[NumberConstants.TWO] = thumDirPath
					+ ApplicationConstants.DELIMITER_FORWARD_SLASH + imageNameArray[NumberConstants.ZERO]
					+ ApplicationConstants.DELIMITER_UNDERSCORE + empId + ".jpg";
		} else {
			writeImageForEmployee = stringArray[NumberConstants.TWO] = thumDirPath
					+ ApplicationConstants.DELIMITER_FORWARD_SLASH + imageNameArray[NumberConstants.ZERO]
					+ ApplicationConstants.DELIMITER_UNDERSCORE + empId + ".jpg";
		}
		return writeImageForEmployee;
	}

	private String getResizeImagePath(String[] stringArray, String[] imageNameArray, String empId, File emp) {
		String writeImageForDevice;
		String empDirPath = DefaultConstants.EMPLOYEE_RESIZE_PATH;
		if (!emp.exists()) {
			emp.mkdirs();
			writeImageForDevice = stringArray[NumberConstants.ONE] = empDirPath
					+ ApplicationConstants.DELIMITER_FORWARD_SLASH + imageNameArray[NumberConstants.ZERO]
					+ ApplicationConstants.DELIMITER_UNDERSCORE + empId + ".jpg";
		} else {
			writeImageForDevice = stringArray[NumberConstants.ONE] = empDirPath
					+ ApplicationConstants.DELIMITER_FORWARD_SLASH + imageNameArray[NumberConstants.ZERO]
					+ ApplicationConstants.DELIMITER_UNDERSCORE + empId + ".jpg";
		}
		return writeImageForDevice;
	}

	private String getOriginalImagePath(String[] stringArray, String[] imageNameArray, String empId, File org) {
		String writeImageForOrginal;
		String orgDirPath = DefaultConstants.EMPLOYEE_ORIGINAL_PATH;
		if (!org.exists()) {
			org.mkdirs();
			writeImageForOrginal = stringArray[NumberConstants.ZERO] = orgDirPath
					+ ApplicationConstants.DELIMITER_FORWARD_SLASH + imageNameArray[NumberConstants.ZERO]
					+ ApplicationConstants.DELIMITER_UNDERSCORE + empId + ".jpg";
		} else {
			writeImageForOrginal = stringArray[NumberConstants.ZERO] = orgDirPath
					+ ApplicationConstants.DELIMITER_FORWARD_SLASH + imageNameArray[NumberConstants.ZERO]
					+ ApplicationConstants.DELIMITER_UNDERSCORE + empId + ".jpg";
		}
		return writeImageForOrginal;
	}

	public BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight)
			throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Thumbnails.of(originalImage).size(targetWidth, targetHeight).outputFormat(ApplicationConstants.FORMAT_JPEG)
				.outputQuality(0.90).toOutputStream(outputStream);
		byte[] data = outputStream.toByteArray();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		return ImageIO.read(inputStream);
	}

	public void saveEmployeeImageWhileEnrolling(MultipartFile file) {
		try {
			List<Image> imageList = new ArrayList<>();

			String fileName = file.getOriginalFilename();

			Employee employee = searchEmployee(fileName);
			List<Employee> employeeList = new ArrayList<Employee>();
			if (null != employee) {
				employeeList.add(employee);

				byte[] bytes = file.getBytes();
				InputStream is = new ByteArrayInputStream(bytes);
				BufferedImage originalImage = ImageIO.read(is);

				String[] imagePath = imageProcessing(originalImage, fileName);
				Image imageObj = imageRepository.findByOriginalPath(imagePath[NumberConstants.ZERO]);
				if (null == imageObj) {
					Image imageSaved = new Image();
					setImagePath(imageList, employeeList, imagePath, imageSaved);
				}
			}

			imageRepository.saveAll(imageList);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void uploadEmployeeImageDirectory(MultipartFile[] files) {
		try {
			List<Image> imageList = new ArrayList<>();
			for (MultipartFile file : files) {

				String fileName = file.getOriginalFilename();
				String[] fileNameArray = fileName.split(ApplicationConstants.DELIMITER_FORWARD_SLASH);
				String imageName = fileNameArray[NumberConstants.ONE];
				Employee employee = searchEmployee(imageName);
				List<Employee> employeeList = new ArrayList<Employee>();
				if (null != employee) {
					employeeList.add(employee);

					byte[] bytes = file.getBytes();
					InputStream is = new ByteArrayInputStream(bytes);
					BufferedImage originalImage = ImageIO.read(is);

					String[] imagePath = imageProcessing(originalImage, imageName);
					Image imageObj = imageRepository.findByOriginalPath(imagePath[NumberConstants.ZERO]);
					if (null == imageObj) {
						Image imageSaved = new Image();
						setImagePath(imageList, employeeList, imagePath, imageSaved);
					}
				}
			}

			imageRepository.saveAll(imageList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setImagePath(List<Image> imageList, List<Employee> employeeList, String[] imagePath,
			Image imageSaved) {
		imageSaved.setEmployee(employeeList);
		imageSaved.setOriginalPath(imagePath[NumberConstants.ZERO]);
		imageSaved.setThumbnailPath(imagePath[NumberConstants.TWO]);
		imageSaved.setResizePath(imagePath[NumberConstants.ONE]);
		imageSaved.setDeleted(false);

		imageList.add(imageSaved);
	}

}
