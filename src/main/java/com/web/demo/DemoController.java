package com.web.demo;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.poi.openxml4j.exceptions.PartAlreadyExistsException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.zxing.WriterException;
import com.web.auth.AuthenticationService;
import com.web.auth.RefferalRequest;
import com.web.auth.UpdateExnessLisaRequest;
import com.web.auth.UpdateExnessRequest;
import com.web.auth.UpdateRefRequest;
import com.web.auth.UpdateRefResponse;
import com.web.dto.AdminPixiuResponse;
import com.web.dto.ChangePasswordRequest;
import com.web.dto.ExnessInfoDto;
import com.web.dto.ExnessResponse;
import com.web.dto.HistoryResponse;
import com.web.dto.InfoLisaResponse;
import com.web.dto.InfoResponse;
import com.web.dto.ManagerPixiuResponse;
import com.web.dto.NetworkDto;
import com.web.dto.PreviousMonthResponse;
import com.web.dto.TwoFARequest;
import com.web.dto.UpdateInfoRequest;
import com.web.dto.UserResponse;
import com.web.exception.NotFoundException;
import com.web.service.CommissionService;
import com.web.service.ExnessService;
import com.web.service.HistoryService;
import com.web.service.ImageUploadService;
import com.web.service.MessageService;
import com.web.service.PrevService;
import com.web.service.TransactionService;
import com.web.service.UserService;
import com.web.user.AdminPixiu;
import com.web.user.Commission;
import com.web.user.Exness;
import com.web.user.ExnessRepository;
import com.web.user.History;
import com.web.user.ManagerPixiu;
import com.web.user.Message;
import com.web.user.Transaction;
import com.web.user.User;
import com.web.user.UserRepository;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/secured")
@CrossOrigin("*")
@Hidden
@RequiredArgsConstructor
public class DemoController {
	private final UserRepository userRepo;
	private final AuthenticationService service;
	private final UserService userService;
	private final MessageService messService;
	private final ExnessService exService;
	private final ExnessRepository exRepo;
	private final HistoryService hisService;
	private final PrevService prevService;
	private final SecretGenerator secretGenerator;
	private final PasswordEncoder passwordEncoder;
	private final QrDataFactory qrDataFactory;
	private final QrGenerator qrGenerator;
	private final TransactionService transactionService;
	private final CommissionService commissService;
	private final ImageUploadService uploadService;

	// cap nhat exness
	@GetMapping("/active-exness/{exness}")
	public ResponseEntity<String> activeExness(@PathVariable("exness") String exness) {
		service.activeExness(exness);
		return ResponseEntity.ok("OK");
	}

	@GetMapping
	public ResponseEntity<String> sayHello() {
		return ResponseEntity.ok("Hello from secured endpoint");
	}

	@GetMapping("/get-all-exness/email={email}")
	public ResponseEntity<List<ExnessResponse>> get(@PathVariable("email") String email) {
		List<ExnessResponse> listExness = service.getAllExness(email);
		return ResponseEntity.ok(listExness);
	}

	@GetMapping("/get-all-exnessLisa/email={email}")
	public ResponseEntity<List<ExnessResponse>> getLisa(@PathVariable("email") String email) {
		List<ExnessResponse> listExness = service.getAllExnessLisa(email);
		return ResponseEntity.ok(listExness);
	}

	@GetMapping("/getHistoryLisa/{email}")
	public ResponseEntity<List<HistoryResponse>> getHistoryByEmailLisa(@PathVariable("email") String email) {
		List<History> listHistories = hisService.findHistoryByReceiver(email);
		List<HistoryResponse> listHistoryResponse = new ArrayList<>();
		for (History history : listHistories) {
			HistoryResponse historyResponse = new HistoryResponse();
			historyResponse.setSender(history.getSender());
			historyResponse.setReceiver(history.getReceiver());
			historyResponse.setAmount(history.getAmount());
			historyResponse.setMessage(history.getMessage());
			historyResponse.setTime(history.getTime());
			historyResponse.setTransaction(history.getTransaction());
			listHistoryResponse.add(historyResponse);
		}

		return ResponseEntity.ok(listHistoryResponse);
	}

	@GetMapping("/get-all-commission-pixiu")
	public ResponseEntity<List<Commission>> getAllCommission() {
		List<Commission> listCommission = commissService.getAllCommissionByBranchName("PixiuGroup");

		return ResponseEntity.ok(listCommission);
	}
	
	@GetMapping("/get-all-commission-pixiu-super")
	public ResponseEntity<List<Commission>> getAllCommissionSuper() {
		List<Commission> listCommission = commissService.getAllCommissionByBranchName("PixiuGroup");

		return ResponseEntity.ok(listCommission);
	}
	
	@GetMapping("/get-all-commission-pixiu-admin/{email}")
	public ResponseEntity<List<AdminPixiu>> getAllCommissionAdmin(@PathVariable("email") String email) {
		List<AdminPixiu> listCommission = commissService.getAllCommissionByBranchNameAdmin("PixiuGroup", email);

		return ResponseEntity.ok(listCommission);
	}
	
	@GetMapping("/get-all-commission-pixiu-manager")
	public ResponseEntity<List<ManagerPixiu>> getAllCommissionManager() {
		List<ManagerPixiu> listCommission = commissService.getAllCommissionByBranchNameManager("PixiuGroup");

		return ResponseEntity.ok(listCommission);
	}

	@GetMapping("/get-all-account")
	public ResponseEntity<List<UserResponse>> getListAccount() {
		List<UserResponse> result = new ArrayList<>();
		List<User> listUsers = userRepo.getUsersByBranchName("PixiuGroup");

		for (User user : listUsers) {
			if (user.getRole().name() != "ADMIN") {
				UserResponse userItem = new UserResponse();
				userItem.setId(user.getId());
				userItem.setEmail(user.getEmail());
				userItem.setName(user.getFirstname() + " " + user.getLastname());
				userItem.setRole(user.getRole().name());
				result.add(userItem);
			}

		}

		return ResponseEntity.ok(result);
	}

	@GetMapping("/get-total-commission/{email}")
	public ResponseEntity<Double> getTotalCommission(@PathVariable("email") String email) {
		double totalCommission = 0.0;
		if (email.equalsIgnoreCase("trantuongthuy@gmail.com")) {
			totalCommission = commissService.getTotalCommission("Alex");
		} else if (email.equalsIgnoreCase("pixiu_group@gmail.com")) {
			totalCommission = commissService.getTotalCommission("PixiuGroup");
		} else {
			throw new NotFoundException("You cann't invoke to this information!");
		}
		return ResponseEntity.ok(totalCommission);
	}

	@GetMapping("/get-prev-data/{email}")
	public ResponseEntity<PreviousMonthResponse> getPreviousMonthData(@PathVariable("email") String email) {
		PreviousMonthResponse result = new PreviousMonthResponse();
		if (email.contains("@")) {
			result = exService.findByEmail(email);
		} else {
			result = exService.findByExness(email);
		}

		return ResponseEntity.ok(result);
	}

//	@PostMapping("/withdraw-ib")
//	public ResponseEntity<String> withdrawIB(@RequestBody WithdrawRequest request) {
//		String message = "[Withdraw] " + request.getEmail() + " rút " + request.getAmount();
//		User user = userRepo.findByEmail(request.getEmail()).get();
//		if (request.getAmount() > user.getBalance() - 0.1) {
//			return ResponseEntity.ok("Không đủ số dư để rút, luôn phải chừa lại 1 cent ~ $0.1");
//		}
//
//		TimeProvider timeProvider = new SystemTimeProvider();
//		CodeGenerator codeGenerator = new DefaultCodeGenerator();
//		DefaultCodeVerifier verify = new DefaultCodeVerifier(codeGenerator, timeProvider);
//		verify.setAllowedTimePeriodDiscrepancy(0);
//
//		if (verify.isValidCode(user.getSecret(), request.getCode())) {
//			user.setBalance(user.getBalance() - request.getAmount());
//			userRepo.save(user);
//
//			Transaction transaction = new Transaction();
//			transaction.setTime(String.valueOf(System.currentTimeMillis() / 1000));
//			transaction.setStatus(0);
//			transaction.setWithdrawer(request.getEmail());
//			transaction.setAmount(request.getAmount());
//			tranRepo.save(transaction);
//
//			tele.sendMessageToChat(Long.parseLong("-1001804531952"), message);
//			return ResponseEntity.ok("Rút thành công!");
//		} else {
//			return ResponseEntity.ok("Mã 2FA không chính xác!");
//		}
//	}

	@GetMapping("/showQR/{email}")
	public List<String> generate2FA(@PathVariable("email") String email)
			throws QrGenerationException, WriterException, IOException, CodeGenerationException {
		Optional<User> user = userRepo.findByEmail(email);
		QrData data = qrDataFactory.newBuilder().label(user.get().getEmail()).secret(user.get().getSecret())
				.issuer("Something Application").period(30).build();

		String qrCodeImage = getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
		List<String> info2FA = new ArrayList<>();
		String isEnabled = "";
		if (user.get().isMfaEnabled()) {
			isEnabled = "true";
		} else {
			isEnabled = "false";
		}
		info2FA.add(isEnabled);
		info2FA.add(qrCodeImage);

		return info2FA;
	}

	@PostMapping("/enable")
	public String enabled(@RequestBody TwoFARequest request) {
		Optional<User> user = userRepo.findByEmail(request.getEmail());
		TimeProvider timeProvider = new SystemTimeProvider();
		CodeGenerator codeGenerator = new DefaultCodeGenerator();
		DefaultCodeVerifier verify = new DefaultCodeVerifier(codeGenerator, timeProvider);
		verify.setAllowedTimePeriodDiscrepancy(0);

		if (verify.isValidCode(user.get().getSecret(), request.getCode())) {
			user.get().setMfaEnabled(true);
			userRepo.save(user.get());
			return "Enabled Success";
		} else {
			return "Enabled Failed";
		}
	}

	@PostMapping("/disable")
	public String disabled(@RequestBody TwoFARequest request) {
		Optional<User> user = userRepo.findByEmail(request.getEmail());
		TimeProvider timeProvider = new SystemTimeProvider();
		CodeGenerator codeGenerator = new DefaultCodeGenerator();
		DefaultCodeVerifier verify = new DefaultCodeVerifier(codeGenerator, timeProvider);
		verify.setAllowedTimePeriodDiscrepancy(0);

		if (verify.isValidCode(user.get().getSecret(), request.getCode())) {
			// xóa secret 2fa
			String secret = secretGenerator.generate();

			user.get().setMfaEnabled(false);
			user.get().setSecret(secret);
			userRepo.save(user.get());
			return "Disabled Success";
		} else {
			return "Disabled Failed";
		}

	}

	@GetMapping("/get-info-by-exness/exness={exnessId}&from={from}&to={to}")
	public ResponseEntity<InfoResponse> getInfoByExness(@PathVariable("exnessId") String exnessId,
			@PathVariable("from") long from, @PathVariable("to") long to) {
		InfoResponse result = new InfoResponse();
		if (exnessId.contains("@")) {
			result = userService.getAllInfoByEmail(exnessId, from, to);
		} else {
			result = userService.getInfoByExnessId(exnessId, from, to);
		}

		return ResponseEntity.ok(result);
	}

	@GetMapping("/get-info-by-exness-time-range/exness={exnessId}&from={from}&to={to}")
	public ResponseEntity<InfoResponse> getInfoTimeRangeByExness(@PathVariable("exnessId") String exnessId,
			@PathVariable("from") long from, @PathVariable("to") long to) {
		InfoResponse result = new InfoResponse();
		if (exnessId.contains("@")) {
			result = userService.getAllInfoByEmail(exnessId, from, to);
		} else {
			result = userService.getInfoFromTimeRangeByExnessId(exnessId, from, to);
		}

		return ResponseEntity.ok(result);
	}
	
	@GetMapping("/get-info-by-exness-time-range-super-admin/exness={exnessId}&from={from}&to={to}")
	public ResponseEntity<InfoResponse> getInfoTimeRangeByExnessBySuperAdmin(@PathVariable("exnessId") String exnessId,
			@PathVariable("from") long from, @PathVariable("to") long to) {
		InfoResponse result = new InfoResponse();
		if (!exnessId.contains("@")) {
			result = userService.getInfoFromTimeRangeByExnessId(exnessId, from, to);
		}

		return ResponseEntity.ok(result);
	}

	@GetMapping("/get-info-by-exness-time-range-admin/exness={exnessId}&from={from}&to={to}")
	public ResponseEntity<AdminPixiuResponse> getInfoTimeRangeByExnessByAdmin(@PathVariable("exnessId") String exnessId,
			@PathVariable("from") long from, @PathVariable("to") long to) {
		AdminPixiuResponse result = new AdminPixiuResponse();
		if (!exnessId.contains("@")) {
			result = userService.getInfoFromTimeRangeByExnessIdByAdmin(exnessId, from, to);
		}

		return ResponseEntity.ok(result);
	}

	@GetMapping("/get-info-by-exness-time-range-manager/exness={exnessId}&from={from}&to={to}")
	public ResponseEntity<ManagerPixiuResponse> getInfoTimeRangeByExnessByManager(@PathVariable("exnessId") String exnessId,
			@PathVariable("from") long from, @PathVariable("to") long to) {
		ManagerPixiuResponse result = new ManagerPixiuResponse();
		if (!exnessId.contains("@")) {
			result = userService.getInfoFromTimeRangeByExnessIdByManager(exnessId, from, to);
		}

		return ResponseEntity.ok(result);
	}

	@GetMapping("/get-info-by-exnessLisa/exness={exnessId}&from={from}&to={to}")
	public ResponseEntity<InfoLisaResponse> getInfoByExnessLisa(@PathVariable("exnessId") String exnessId,
			@PathVariable("from") long from, @PathVariable("to") long to) {
		InfoLisaResponse result = new InfoLisaResponse();
		if (exnessId.contains("@")) {
			result = userService.getAllInfoByEmailLisa(exnessId, from, to);
		} else {
			result = userService.getInfoByExnessLisa(exnessId, from, to);
		}

		return ResponseEntity.ok(result);
	}

	@PostMapping("/upload-transaction")
	public ResponseEntity<String> uploadTransaction(@RequestParam("file") MultipartFile file,
			@RequestParam("exness") String exness) {
		Optional<Exness> exnessQuery = exService.findByExnessId(exness);
		if (exnessQuery.isEmpty()) {
			throw new NotFoundException("This exness is not existed!");
		}
		String fileName = "transaction_exxness_id" + exness;
		String url = uploadService.uploadImage(file, fileName);

		if (url != null) {
			exnessQuery.get().setMessage(url);
			exRepo.save(exnessQuery.get());

			// String message = "Exness ID: " + exness + " đã cập nhật ảnh chuyển tiền!";
			// tele.sendMessageToChat(chatId, message);
			return ResponseEntity.ok(url);
		} else {
			return ResponseEntity.ok("Error");
		}
	}

	@GetMapping("/getNetwork/{email}")
	public ResponseEntity<List<NetworkDto>> getNetworkLevel(@PathVariable("email") String email) {
		int level = 1;
		int root = 1;
		List<NetworkDto> network = new ArrayList<>();
		getUserNetwork(email, level, root, network);

		Collections.sort(network);
		return ResponseEntity.ok(network);
	}

	@SuppressWarnings("resource")
	@PostMapping("/shareIB")
	public ResponseEntity<String> shareIB(@RequestParam("file") MultipartFile file) {
		if (file.isEmpty()) {
			return ResponseEntity.ok("Bạn chưa đính kèm file dữ liệu");
		}

		HashMap<Integer, String> data = new HashMap<>();
		InputStream inputStream = null;
		Workbook workbook = null;

		try {
			// Đọc tệp Excel
			inputStream = file.getInputStream();
			workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(0); // Chọn sheet cần đọc dữ liệu

			Row headerRow = sheet.getRow(0);
			if (headerRow == null) {
				return ResponseEntity.ok("File không đúng định dạng (dữ liệu trống)");
			} else if (headerRow.getPhysicalNumberOfCells() != 16) {
				return ResponseEntity.ok("File không đúng định dạng (16 cột)");
			}

			String idHeader = getCellValueAsString(headerRow.getCell(0));
			String rewardHeader = getCellValueAsString(headerRow.getCell(9));
			String exnessIdHeader = getCellValueAsString(headerRow.getCell(14));

			if (!"id".equals(idHeader)) {
				return ResponseEntity.ok("File không đúng định dạng (cột thứ 1 không phải là id)");
			}

			if (!"reward".equals(rewardHeader)) {
				return ResponseEntity.ok("File không đúng định dạng (cột thứ 10 không phải là reward)");
			}

			if (!"client_account".equals(exnessIdHeader)) {
				return ResponseEntity
						.ok("File không đúng định dạng (cột thứ 15 không phải là client_account - Exness ID)");
			}

			// Lặp qua từng dòng (bắt đầu từ dòng thứ 2, do dòng đầu tiên là tiêu đề)
			for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row row = sheet.getRow(rowIndex);

				// Đọc giá trị từ cột thứ 4, 5 và 7
				Cell cellTransaction = row.getCell(0);
				Cell cellIB = row.getCell(9); // Cột thứ 5 (index 4)
				Cell cellExnessId = row.getCell(14); // Cột thứ 7 (index 6)

				// Kiểm tra xem cell4, cell5 và cell7 có dữ liệu không
				if (cellTransaction != null && cellIB != null && cellExnessId != null) {
					String ibTransaction = getCellValueAsString(cellTransaction);
					String ibReward = getCellValueAsString(cellIB);
					String exnessIdValue = getCellValueAsString(cellExnessId);
					if (exnessIdValue.contains("E") || ibTransaction.contains("E")) {
						// Xử lý giá trị số thập phân với dấu phẩy
						double exnessIdDouble = Double.parseDouble(exnessIdValue);
						long exnessIdLong = (long) exnessIdDouble;
						exnessIdValue = String.valueOf(exnessIdLong);

						double exnessTransactionDouble = Double.parseDouble(ibTransaction);
						long exnessTransactionLong = (long) exnessTransactionDouble;
						ibTransaction = String.valueOf(exnessTransactionLong);
					}
					String value = ibTransaction + "-" + ibReward + "-" + exnessIdValue;
					data.put(rowIndex, value);

				}
			}
			workbook.close();
			inputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.ok("Lỗi khi đọc file!");
		} catch (PartAlreadyExistsException pae) {
			System.out.println(pae);
			return ResponseEntity.ok("File ở chế độ Protected!");
		}

		data.forEach((key, value) -> {
			int firstDashIndex = value.indexOf('-');
			int secondDashIndex = value.indexOf('-', firstDashIndex + 1);
			String exnessTransaction = value.substring(0, firstDashIndex);
			String exnessId = value.substring(secondDashIndex + 1, value.length());
			double amount = Double.parseDouble(value.substring(firstDashIndex + 1, secondDashIndex));
			double amountToInvest = 0, amountToDev = 0;
//			int userLevel = exService.findUserByExness(exnessId).getLevel();
//			if (userLevel == 1) {
//				amountToInvest = amount * 0.5;
//				amountToDev = amount - amountToInvest;
//			} else if (userLevel == 2) {
//
//			}

		});

		return ResponseEntity.ok("OK");
	}

	@GetMapping("/get-message/email={email}")
	public ResponseEntity<List<Message>> getMessage(@PathVariable("email") String email) {
		List<Message> listMessages = messService.findMessagesByEmail(email);
		return ResponseEntity.ok(listMessages);
	}

	@GetMapping("/toggle-message/id={id}")
	public ResponseEntity<String> toggleMessage(@PathVariable("id") long id) {
		messService.toggleMessageStatus(id);
		return ResponseEntity.ok("OK");
	}

	@PostMapping("/edit-info")
	public ResponseEntity<String> editInfo(@RequestBody UpdateInfoRequest request) {
		service.editInfo(request);
		return ResponseEntity.ok("OK");
	}

	private void getUserNetwork(String email, int desiredLevel, int currentLevel, List<NetworkDto> network) {
		if (currentLevel <= desiredLevel) {
			List<User> users = userRepo.findByRefferal(email);
			if (users.isEmpty()) {
				return;
			}

			for (User user : users) {
				String image = "";
				if (user.getImage() == "") {
					image = "/assets/images/avatars/avatar_default.png";
				} else {
					image = user.getImage();
				}
				double profit = exService.getProfitByEmail(user.getEmail());
				double commission = user.getCommission();
				network.add(
						new NetworkDto(user.getEmail(), user.getRefferal(), image, commission, profit, currentLevel));
				getUserNetwork(user.getEmail(), desiredLevel, currentLevel + 1, network);
			}
		}

	}

	@PostMapping("/update-ref")
	public ResponseEntity<UpdateRefResponse> updateRef(@RequestBody UpdateRefRequest request) {
		return ResponseEntity.ok(service.updateRef(request.getCurrent(), request.getCode()));
	}

	@PostMapping("/update-exness")
	public ResponseEntity<UpdateRefResponse> updateExness(@RequestBody UpdateExnessRequest request) {
		return ResponseEntity.ok(service.updateExness(request.getEmail(), request.getExness(), request.getType()));
	}

	@PostMapping("/update-exnessLisa")
	public ResponseEntity<UpdateRefResponse> updateExness(@RequestBody UpdateExnessLisaRequest request) {
		return ResponseEntity.ok(service.updateExnessLisa(request));
	}

	@GetMapping("/get-exness/exness={exness}")
	public ResponseEntity<Exness> getExnessByExnessid(@PathVariable("exness") String exness) {
		return ResponseEntity.ok(exService.findByExnessId(exness).orElse(null));
	}

	@GetMapping("/get-exness/{email}")
	public ResponseEntity<List<String>> getExnessByEmail(@PathVariable("email") String email) {
		return ResponseEntity.ok(service.getExnessByEmail(email));
	}

	@GetMapping("/get-exness-pixiu/{email}/{currentEmail}")
	public ResponseEntity<List<ExnessInfoDto>> getExnessByEmailPixiu(@PathVariable("email") String email, @PathVariable("currentEmail") String currentEmail) {
		if (currentEmail.equalsIgnoreCase("admin_dn@gmail.com")) {
			List<ExnessInfoDto> result = new ArrayList<>();
			User user = userRepo.getByEmail(currentEmail);
			result = service.filterForSubBranch1(result, user);
			return ResponseEntity.ok(result);
		} else {
			return ResponseEntity.ok(service.getExnessByEmailPixiu(email));
		}
		
	}

	@GetMapping("/get-info/id={id}")
	public ResponseEntity<String> getRole(@PathVariable("id") int id) {
		User user = userRepo.getById(id);
		return ResponseEntity.ok(user.getRole().name());
	}

	@GetMapping("/change-role/id={id}/role={role}")
	public ResponseEntity<String> getRole(@PathVariable("id") int id, @PathVariable("role") String role) {
		User user = userRepo.getById(id);
		if (role.equalsIgnoreCase("user")) {
			user.setRole(user.getRole().USER);
		} else if (role.equalsIgnoreCase("manager")) {
			user.setRole(user.getRole().MANAGER);
		} else if (role.equalsIgnoreCase("admin")) {
			user.setRole(user.getRole().ADMIN);
		}
		userRepo.save(user);
		return ResponseEntity.ok("ok");
	}

	@PostMapping("/get-info")
	public ResponseEntity<HashMap<String, String>> getInfo(@RequestBody RefferalRequest request) {
		return ResponseEntity.ok(service.getInfo(request.getEmail()));
	}

	@PostMapping("/upload-avatar")
	public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file,
			@RequestParam("email") String email) {
		User user = userRepo.findByEmail(email).get();

		String fileName = "avatar/user_id_" + user.getId();
		String url = uploadService.uploadImage(file, fileName);
		user.setImage(url);
		userRepo.save(user);
		return ResponseEntity.ok(url);

//		User user = userRepo.findByEmail(email).get();
//
//		try {
//			// Kiểm tra kiểu MIME của tệp
//			String contentType = file.getContentType();
//			if (!contentType.startsWith("image")) {
//				throw new NotFoundException("No image found");
//			}
//
//			// Lấy đường dẫn đến thư mục lưu trữ avatar (src/main/resources/assets/avatar)
//			String uploadDirectory = "src/main/resources/assets/avatar";
//			Path uploadPath = Path.of(uploadDirectory);
//
//			// Tạo thư mục nếu nó chưa tồn tại
//			if (!Files.exists(uploadPath)) {
//				Files.createDirectories(uploadPath);
//			}
//
//			// Lấy tên tệp từ MultipartFile
//			String fileName = "avatar_user_id_" + user.getId() + ".png";
//			Path filePath = uploadPath.resolve(fileName);
//
//			// Lưu tệp vào thư mục
//			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//
//			// Trả về thông báo thành công
//			// Đọc nội dung tệp ảnh
//			byte[] imageBytes = Files.readAllBytes(filePath);
//			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG) // Đặt kiểu MIME cho ảnh (png hoặc phù hợp với
//																		// định dạng ảnh của bạn)
//					.body(imageBytes);
//		} catch (IOException e) {
//			return ResponseEntity.notFound().build();
//		}
	}

	@GetMapping("/avatar/{email}")
	public ResponseEntity<String> getAvatar(@PathVariable("email") String email) {
		User user = userRepo.findByEmail(email).get();
		return ResponseEntity.ok(user.getImage());
		// // Lấy đường dẫn đến thư mục lưu trữ avatar
		// (src/main/resources/assets/avatar)
//		String uploadDirectory = "src/main/resources/assets/avatar";
//		Path uploadPath = Path.of(uploadDirectory);
//
//		User user = userRepo.findByEmail(email).get();
//		// Xây dựng tên tệp dựa trên id
//		String fileName = "avatar_user_id_" + user.getId() + ".png";
//		Path filePath = uploadPath.resolve(fileName);
//
//		try {
//			// Đọc nội dung tệp ảnh
//			byte[] imageBytes = Files.readAllBytes(filePath);
//			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG) // Đặt kiểu MIME cho ảnh (png hoặc phù hợp với
//																		// định dạng ảnh của bạn)
//					.body(imageBytes);
//		} catch (IOException e) {
//			return ResponseEntity.notFound().build();
//		}
	}

	@PostMapping("/upload-banner")
	public ResponseEntity<byte[]> uploadBanner(@RequestParam("file") MultipartFile file) {
		try {
			// Kiểm tra kiểu MIME của tệp
			String contentType = file.getContentType();
			if (!contentType.startsWith("image")) {
				throw new NotFoundException("No image found");
			}

			// Lấy đường dẫn đến thư mục lưu trữ avatar (src/main/resources/assets/avatar)
			String uploadDirectory = "src/main/resources/assets/banner";
			Path uploadPath = Path.of(uploadDirectory);

			// Tạo thư mục nếu nó chưa tồn tại
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			// Lấy tên tệp từ MultipartFile
			String fileName = "banner.png";
			Path filePath = uploadPath.resolve(fileName);

			// Lưu tệp vào thư mục
			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			// Trả về thông báo thành công
			// Đọc nội dung tệp ảnh
			byte[] imageBytes = Files.readAllBytes(filePath);
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG) // Đặt kiểu MIME cho ảnh (png hoặc phù hợp với
																		// định dạng ảnh của bạn)
					.body(imageBytes);
		} catch (IOException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/banner")
	public ResponseEntity<byte[]> getBanner() {
		// Lấy đường dẫn đến thư mục lưu trữ avatar (src/main/resources/assets/avatar)
		String uploadDirectory = "src/main/resources/assets/banner";
		Path uploadPath = Path.of(uploadDirectory);

		// Xây dựng tên tệp dựa trên id
		String fileName = "banner.png";
		Path filePath = uploadPath.resolve(fileName);

		try {
			// Đọc nội dung tệp ảnh
			byte[] imageBytes = Files.readAllBytes(filePath);
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG) // Đặt kiểu MIME cho ảnh (png hoặc phù hợp với
																		// định dạng ảnh của bạn)
					.body(imageBytes);
		} catch (IOException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/change-password")
	public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
		Optional<User> user = userRepo.findByEmail(request.getEmail());
		if (user.isEmpty()) {
			return ResponseEntity.ok("Tài khoản không tồn tại!");
		}

		TimeProvider timeProvider = new SystemTimeProvider();
		CodeGenerator codeGenerator = new DefaultCodeGenerator();
		DefaultCodeVerifier verify = new DefaultCodeVerifier(codeGenerator, timeProvider);
		verify.setAllowedTimePeriodDiscrepancy(0);

		if (verify.isValidCode(user.get().getSecret(), request.getCode())) {
			user.get().setPassword(passwordEncoder.encode(request.getPassword()));
			userRepo.save(user.get());
			return ResponseEntity.ok("Thay đổi mật khẩu thành công!");
		} else {
			return ResponseEntity.ok("Mã 2FA không chính xác!");
		}
	}

	@GetMapping("/get-transaction/email={email}")
	public ResponseEntity<List<Transaction>> getTransactionByEmail(@PathVariable("email") String email) {
		return ResponseEntity.ok(transactionService.findTransactionByEmail(email));
	}

	@GetMapping("/get-history/email={email}")
	public ResponseEntity<List<History>> getHistoryByEmail(@PathVariable("email") String email) {
		return ResponseEntity.ok(hisService.findHistoryByEmail(email));
	}

	private String getCellValueAsString(Cell cell) {
		if (cell == null) {
			return "Ô dữ liệu trống!";
		}

		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue();
		case NUMERIC:
			return String.valueOf(cell.getNumericCellValue());
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		default:
			return "Lỗi! Không thể đọc dữ liệu";
		}
	}

}
