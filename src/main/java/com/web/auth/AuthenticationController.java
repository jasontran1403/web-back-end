package com.web.auth;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.dto.AuthResponse;
import com.web.dto.DataItem;
import com.web.dto.LevelDto;
import com.web.dto.LoginRequest;
import com.web.dto.MessageRequest;
import com.web.dto.RealtimeDataDto;
import com.web.dto.RealtimeDataProjection;
import com.web.dto.RealtimeDto;
import com.web.dto.TestDataDto;
import com.web.dto.UpcomingCandleDto;
import com.web.exception.ExistedException;
import com.web.exception.NotFoundException;
import com.web.service.BalanceService;
import com.web.service.CommissionService;
import com.web.service.ExnessService;
import com.web.service.HistoryService;
import com.web.service.MessageService;
import com.web.service.Mq4DataService;
import com.web.service.ProfitService;
import com.web.service.TransactionService;
import com.web.service.UserService;
import com.web.user.AdminPixiu;
import com.web.user.AdminPixiuRepository;
import com.web.user.Balance;
import com.web.user.Commission;
import com.web.user.Exness;
import com.web.user.ExnessRepository;
import com.web.user.ExnessTransaction;
import com.web.user.ExnessTransactionRepository;
import com.web.user.History;
import com.web.user.ManagerPixiu;
import com.web.user.ManagerPixiuRepository;
import com.web.user.Profit;
import com.web.user.Transaction;
import com.web.user.User;
import com.web.user.UserRepository;
import com.web.utils.CalibrateBracketIB;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AuthenticationController {
	private final UserRepository userRepo;
	private final AuthenticationService service;
	private final HistoryService hisService;
	private final ExnessTransactionRepository exTranRepo;
	private final UserService userService;
	private final ExnessService exService;
	private final MessageService messService;
	private final ExnessRepository exRepo;
	private final TransactionService tranService;
	private final ProfitService proService;
	private final BalanceService balanceService;
	private final CommissionService commissService;
	private final ManagerPixiuRepository managerRepo;
	private final AdminPixiuRepository adminRepo;
	private final Mq4DataService mq4Service;

	private final CalibrateBracketIB utils;

	@GetMapping("/real-time-data/latest")
	public ResponseEntity<String> getLatestData() {
		long latestTransaction = mq4Service.getLatestTransaction();
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");

		// Chuyển đổi timestamp thành đối tượng Date
		Date date = new Date(latestTransaction * 1000); // *1000 để đổi về milliseconds

		// Chuyển đối tượng Date thành chuỗi với định dạng "yyyy-MM-dd"
		String formattedDate = dateFormat.format(date);
		return ResponseEntity.ok(formattedDate);
	}

	@GetMapping("/real-time-data")
	public ResponseEntity<List<RealtimeDataProjection>> realtimeDataTransfer() {
		return ResponseEntity.ok(mq4Service.getRealtimeData());
	}

	@GetMapping("/real-time-data/{exnessId}/{currencyName}")
	public ResponseEntity<String> realtimeDataCandle(@PathVariable("exnessId") String exnessId,
			@PathVariable("currencyName") String currencyName) {
		return ResponseEntity.ok(mq4Service.getRealtimeCandle(exnessId, currencyName));
	}

	@GetMapping("/real-time-candle/{exnessId}/{currencyName}")
	public ResponseEntity<String> realtimeDataCandleUpcoming(@PathVariable("exnessId") String exnessId,
			@PathVariable("currencyName") String currencyName) {
		return ResponseEntity.ok(mq4Service.getUpcomingCandle(exnessId, currencyName));
	}

	@GetMapping("/real-time-data/{exnessId}")
	public ResponseEntity<RealtimeDataDto> realtimeDataTransferByExnessId(@PathVariable("exnessId") String exnessId) {
		return ResponseEntity.ok(mq4Service.getRealtimeDataByExnessId(exnessId));
	}

	@PostMapping("/real-time-candle")
	public ResponseEntity<String> realtimeCandleHandling(@RequestBody UpcomingCandleDto upcomingCandle) {
		mq4Service.saveUpcomingCandle(upcomingCandle);
		return ResponseEntity.ok("ok");
	}

	@PostMapping("/real-time-data")
	public ResponseEntity<String> realtimeHandling(@RequestBody RealtimeDto realtimeDto) {
		mq4Service.saveData(realtimeDto);
		return ResponseEntity.ok("ok");
	}

	@GetMapping("/distributed/{totalCapital}/{accountCapital}/{totalIB}")
	public ResponseEntity<Map<String, List<Double>>> test4(@PathVariable("totalCapital") double totalCapital,
			@PathVariable("accountCapital") double accountCapital, @PathVariable("totalIB") double totalIB) {

		Map<String, List<Double>> result2 = utils.calculateDistributedIB("130124124", totalCapital, accountCapital,
				totalIB, "ADMIN");

		return ResponseEntity.ok(result2);
	}

	@GetMapping("/pixiu-group/day={day}")
	public ResponseEntity<List<DataItem>> shareIB(@PathVariable("day") int day)
			throws JsonMappingException, JsonProcessingException {
		Date currentDateTime = new Date();

		// Lấy ngày hiện tại
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(currentDateTime);

		// Đặt thời gian thành 00:00:01
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 1);

		// Lấy timestamp sau khi đặt thời gian
		long timestamp = calendar.getTimeInMillis() / 1000 - (86400 * day);

		List<DataItem> listData2 = getPixiuIB(timestamp);

		double totalIBSubBranch1 = 0, totalIBSubBranch2 = 0;

		double totalCapitalFromPixiu = tranService.getTotalDepositFromPixiu(timestamp) / 100;

		List<Exness> listExnessFromPixiu = exService.getListExnessByBranchName("PixiuGroup");
		User rootUserSubBranch1 = userRepo.getByEmail("pixiu@gmail.com");
		List<Exness> listExnessFromPixiuSub1 = utils.filterForSubBranch1(listExnessFromPixiu, rootUserSubBranch1);

		User rootUserSubBranch2 = userRepo.getByEmail("admin_dn@gmail.com");
		List<Exness> listExnessFromPixiuSub2 = utils.filterForSubBranch1(listExnessFromPixiu, rootUserSubBranch2);

		for (DataItem item : listData2) {
			for (Exness exness : listExnessFromPixiuSub1) {
				if (exness.getExness().equalsIgnoreCase(String.valueOf(item.getClient_account()))) {
					totalIBSubBranch1 += Double.parseDouble(item.getReward());
				}
			}

			for (Exness exness : listExnessFromPixiuSub2) {
				if (exness.getExness().equalsIgnoreCase(String.valueOf(item.getClient_account()))) {
					double amount = Double.parseDouble(item.getReward());
					if (amount > 0) {
						String messageForAdmin = "";
						messageForAdmin += "Exness ID#" + exness.getExness() + " được hưởng phần chia= " + amount * 0.6
								+ " từ tổng IB= " + amount * 0.8;

						AdminPixiu adminShareIB = new AdminPixiu();
						adminShareIB.setAmount(amount * 0.6);
						adminShareIB.setExnessId(exness.getExness());
						adminShareIB.setTime(timestamp);
						adminShareIB.setMessage(messageForAdmin);

						adminRepo.save(adminShareIB);
					}
				}
			}

			if (Double.parseDouble(item.getReward()) > 0) {
				Commission commissionSubBranch1 = new Commission();
				commissionSubBranch1.setAmount(Double.parseDouble(item.getReward()) * 0.8);
				commissionSubBranch1.setExnessId(String.valueOf(item.getClient_account()));
				commissionSubBranch1.setTime(timestamp);
				commissionSubBranch1.setTransactionId(item.getClient_uid());
				commissionSubBranch1.setMessage("20% của " + Double.parseDouble(item.getReward()) + " chia cho LP="
						+ Double.parseDouble(item.getReward()) * 0.2);

				commissService.saveCommission(commissionSubBranch1);
			}
		}

		for (Exness item : listExnessFromPixiuSub1) {
			double accountCapital = tranService.getTotalDepositByExnessId(item.getExness()) / 100;
			if (item.getUser().getRole().name() == "MANAGER") {

				Map<String, List<Double>> resultForLeader = utils.calculateDistributedIB(item.getExness(),
						totalCapitalFromPixiu, accountCapital, totalIBSubBranch1, "MANAGER");
				Map<String, List<Double>> resultForAdmin = utils.calculateDistributedIB(item.getExness(),
						totalCapitalFromPixiu, accountCapital, totalIBSubBranch1, "ADMIN");
				if (resultForLeader.get(item.getExness()).size() > 0) {
					double amount = resultForLeader.get(item.getExness()).stream().reduce(0.0, Double::sum);
					String messageForLeader = "";
					messageForLeader += "IB= " + totalIBSubBranch1 + " - Tổng DS= " + totalCapitalFromPixiu
							+ " - DSCN= " + accountCapital;
					messageForLeader += "IB mốc 1 = " + resultForLeader.get(item.getExness()).get(0);
					messageForLeader += "IB mốc 2 (" + 0.2 + ")= " + resultForLeader.get(item.getExness()).get(1);
					messageForLeader += "IB mốc 3 (" + 0.4 + ")= " + resultForLeader.get(item.getExness()).get(2);
					messageForLeader += "IB mốc 4 (" + 0.6 + ")= " + resultForLeader.get(item.getExness()).get(3);

					if (amount > 0) {
						ManagerPixiu managerShareIB = new ManagerPixiu();
						managerShareIB.setAmount(amount);
						managerShareIB.setExnessId(item.getExness());
						managerShareIB.setTime(timestamp);
						managerShareIB.setMessage(messageForLeader);

						managerRepo.save(managerShareIB);

						Exness exnessToUpdate = exService.findByExnessId(item.getExness()).get();
						User user = exnessToUpdate.getUser();
						user.setCommission(user.getCommission() + amount);
						userRepo.save(user);
					}
				}

				if (resultForAdmin.get(item.getExness()).size() > 0) {
					double amount = resultForAdmin.get(item.getExness()).stream().reduce(0.0, Double::sum);
					String messageForAdmin = "";
					messageForAdmin += "IB= " + totalIBSubBranch1 + " - Tổng DS= " + totalCapitalFromPixiu + " - DSCN= "
							+ accountCapital;
					messageForAdmin += "IB mốc 1 = " + resultForAdmin.get(item.getExness()).get(0);
					messageForAdmin += "IB mốc 2 (" + 0.3 + ")= " + resultForAdmin.get(item.getExness()).get(1);
					messageForAdmin += "IB mốc 3 (" + 0.5 + ")= " + resultForLeader.get(item.getExness()).get(2);
					messageForAdmin += "IB mốc 4 (" + 0.7 + ")= " + resultForAdmin.get(item.getExness()).get(3);

					if (amount > 0) {
						AdminPixiu adminShareIB = new AdminPixiu();
						adminShareIB.setAmount(amount);
						adminShareIB.setExnessId(item.getExness());
						adminShareIB.setTime(timestamp);
						adminShareIB.setMessage(messageForAdmin);

						adminRepo.save(adminShareIB);

						Exness exnessToUpdate = exService.findByExnessId(item.getExness()).get();
						User user = exnessToUpdate.getUser();
						user.setCommission(user.getCommission() + amount);
						userRepo.save(user);
					}
				}
			}
		}

		return ResponseEntity.ok(listData2);
	}

	@GetMapping("/pixiu-group/delete")
	public ResponseEntity<String> detele() {
		commissService.deleteAllCommission();

		return ResponseEntity.ok("ok");
	}

	private List<TestDataDto> generateTestData() {
		List<TestDataDto> result = new ArrayList<>();
		List<Exness> exnesses = new ArrayList<>();

		List<User> userFromPixiu = userService.getUsersByBranchName("PixiuGroup");
		for (User user : userFromPixiu) {
			List<Exness> exnessByUser = user.getExnessList();
			for (Exness exness : exnessByUser) {
				exnesses.add(exness);
			}
		}

		for (Exness exness : exnesses) {
			TestDataDto item = new TestDataDto();
			item.setExnessId(exness.getExness());
			// Tạo số ngẫu nhiên là số thập phân từ 5 đến 100
			double randomIb = 5 + Math.random() * (100 - 5);
			item.setIb(randomIb);
			result.add(item);
		}

		return result;
	}

	private List<DataItem> getPixiuIB(long dateTime) throws JsonMappingException, JsonProcessingException {
		List<DataItem> results = new ArrayList<>();

		Date currentDateTime = new Date();

		// Lấy ngày hiện tại
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(currentDateTime);

		// Đặt thời gian thành 00:00:01
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 1);

		// Lấy timestamp sau khi đặt thời gian
		long timestamp = calendar.getTimeInMillis() / 1000 - 86400;

		// Tạo đối tượng SimpleDateFormat với định dạng "yyyy-MM-dd"
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		// Chuyển đổi timestamp thành đối tượng Date
		Date date = new Date(dateTime * 1000); // *1000 để đổi về milliseconds

		// Chuyển đối tượng Date thành chuỗi với định dạng "yyyy-MM-dd"
		String formattedDate = dateFormat.format(date);

		String url = "https://my.exnessaffiliates.com/api/v2/auth/";

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("Accept", "application/json");

		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setLogin("Long_phan@ymail.com");
		loginRequest.setPassword("Xitrum11");

		HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

		ResponseEntity<AuthResponse> responseEntity = new RestTemplate().exchange(url, HttpMethod.POST, request,
				AuthResponse.class);
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			AuthResponse authResponse = responseEntity.getBody();
			String token = authResponse.getToken();

			// Gọi API khác với token
			// Ví dụ: Gửi yêu cầu GET đến một API sử dụng token
			String apiUrl = "https://my.exaffiliates.com/api/reports/rewards/?limit=1000&reward_date_from="
					+ formattedDate + "&reward_date_to=" + formattedDate;

			HttpHeaders headersWithToken = new HttpHeaders();
			headersWithToken.set("Authorization", "JWT " + token);

			HttpEntity<String> requestWithToken = new HttpEntity<>(headersWithToken);

			ResponseEntity<String> apiResponse = new RestTemplate().exchange(apiUrl, HttpMethod.GET, requestWithToken,
					String.class);
			List<DataItem> dataItems = new ArrayList<>();
			String json = apiResponse.getBody();

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(json); // Chuyển JSON thành một đối tượng JsonNode

			if (rootNode.has("data")) {
				JsonNode dataNode = rootNode.get("data");
				if (dataNode.isArray()) {
					dataItems = objectMapper.readValue(dataNode.toString(), new TypeReference<List<DataItem>>() {
					});
				}
			}

			for (DataItem item : dataItems) {
				Optional<Exness> exness = exService.findByExnessId(String.valueOf(item.getClient_account()));
				if (exness.isPresent()) {
					if (exness.get().getUser().getBranchName().equals("PixiuGroup")) {
						results.add(item);
					}
				}

			}

		}

		return results;
	}

	@GetMapping("/test2/{date}")
	public ResponseEntity<String> testPixiu(@PathVariable("date") String date)
			throws JsonMappingException, JsonProcessingException {
		Date currentDateTime = new Date();

		// Lấy ngày hiện tại
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(currentDateTime);

		// Đặt thời gian thành 00:00:01
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 1);

		// Lấy timestamp sau khi đặt thời gian
		long timestamp = calendar.getTimeInMillis() / 1000 - 86400;

		// Tạo đối tượng SimpleDateFormat với định dạng "yyyy-MM-dd"
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		// Chuyển đổi timestamp thành đối tượng Date
		Date date2 = new Date(timestamp * 1000); // *1000 để đổi về milliseconds

		// Chuyển đối tượng Date thành chuỗi với định dạng "yyyy-MM-dd"
		String formattedDate = dateFormat.format(date2);

		String url = "https://my.exnessaffiliates.com/api/v2/auth/";

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("Accept", "application/json");

		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setLogin("Long_phan@ymail.com");
		loginRequest.setPassword("Xitrum11");

		String test = "";

		HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);
		double maxAmount = 0;
		ResponseEntity<AuthResponse> responseEntity = new RestTemplate().exchange(url, HttpMethod.POST, request,
				AuthResponse.class);
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			AuthResponse authResponse = responseEntity.getBody();
			String token = authResponse.getToken();

			// Gọi API khác với token
			// Ví dụ: Gửi yêu cầu GET đến một API sử dụng token
			String apiUrl = "https://my.exaffiliates.com/api/reports/rewards/?reward_date_from=" + date
					+ "&reward_date_to=" + date;

			HttpHeaders headersWithToken = new HttpHeaders();
			headersWithToken.set("Authorization", "JWT " + token);

			HttpEntity<String> requestWithToken = new HttpEntity<>(headersWithToken);

			ResponseEntity<String> apiResponse = new RestTemplate().exchange(apiUrl, HttpMethod.GET, requestWithToken,
					String.class);
			List<DataItem> dataItems = new ArrayList<>();
			String json = apiResponse.getBody();

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(json); // Chuyển JSON thành một đối tượng JsonNode

			if (rootNode.has("data")) {
				JsonNode dataNode = rootNode.get("data");
				if (dataNode.isArray()) {
					dataItems = objectMapper.readValue(dataNode.toString(), new TypeReference<List<DataItem>>() {
					});
				}
			}

			System.out.println(dataItems);

			for (DataItem item : dataItems) {
				Long clientAccount = item.getClient_account();
				Optional<Exness> exness = exService.findByExnessId(String.valueOf(clientAccount));

				if (exness.isEmpty()) {
					continue;
				}

//				1.58
//				16.07
				CalibrateBracketIB utils = new CalibrateBracketIB(exService, tranService, userService);
				Map<String, LevelDto> results = utils.calibrateIBBracket();
				if (results.containsKey(exness.get().getExness())) {
					double rate = results.get(exness.get().getExness()).getRate();
					Double rewardUsd = Double.parseDouble(item.getReward_usd());
					maxAmount = Math.max(maxAmount, results.get(exness.get().getExness()).getSales());
					if (rate > 0) {
						Commission commission = new Commission();
						commission.setAmount(rewardUsd * rate);
						String scientificNotation = String.valueOf(results.get(exness.get().getExness()).getSales());

						// Chuyển đổi sang số bình thường
						double normalNumber = Double.parseDouble(scientificNotation);

						// Định dạng số bình thường
						DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
						System.out.println(results.get(exness.get().getExness()).getExnessId() + " - "
								+ results.get(exness.get().getExness()).getRate() + " - "
								+ decimalFormat.format(normalNumber));
						commission.setExnessId(exness.get().getExness());
						commission.setTransactionId(item.getClient_uid());
						commission.setTime(timestamp);

						try {
							commissService.saveCommission(commission);
						} catch (Exception e) {
							e.printStackTrace();
						}

						userService.updateTotalCommission(exness.get().getUser(), rewardUsd);
					}

				}
			}
		}

		String scientificNotation = String.valueOf(maxAmount);

		// Chuyển đổi sang số bình thường
		double normalNumber = Double.parseDouble(scientificNotation);

		// Định dạng số bình thường
		DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

		return ResponseEntity.ok(String.valueOf(decimalFormat.format(normalNumber)));
	}

	@GetMapping("/updateProfit")
	public ResponseEntity<String> updateTotalProfit() {
		List<Exness> listExness = exService.findAllExness();
		for (Exness exness : listExness) {
			double sumAmountProfit = proService.sumTotalProfit(exness.getExness());
			exService.fixTotalProfit(exness.getExness(), sumAmountProfit);
		}

		return ResponseEntity.ok("ok");
	}

	@PostMapping("/test-mess")
	public ResponseEntity<String> testMessage(@RequestBody MessageRequest request) {
		messService.saveMessage(request);
		return ResponseEntity.ok("OK");
	}

	@GetMapping("/test/{branchName}")
	public ResponseEntity<List<User>> test(@PathVariable("branchName") String branchName) {
		List<User> listUsers = userService.getUsersByBranchName(branchName);
		if (listUsers.size() == 0) {
			throw new NotFoundException("This branch name: " + branchName + " is not existed!");
		}
		return ResponseEntity.ok(listUsers);
	}

	// check co trong he thong hay
	// curl -X GET --header 'Accept: application/json' --header 'Authorization: JWT
	// eyJhbGciOiJSUzI1NiIsImtpZCI6InVzZXIiLCJ0eXAiOiJKV1QifQ.eyJqdGkiOiIyNGNiZDE0OTk0ZDg0ZjRkODk3OGE2YjY3YmQ4YTFmMiIsImV4cCI6MTY5ODY3MDAyNywiaXNzIjoiQXV0aGVudGljYXRpb24iLCJpYXQiOjE2OTg2NDg0MjcsInN1YiI6IjViYjhhYWE5MjExYTQwMTRiOGZiYjViNjNmYmY5NDA1IiwiYXVkIjpbInBhcnRuZXJzaGlwIl0sImFkZGl0aW9uYWxfcGFyYW1zIjp7IndsX2lkIjoiODcxMWI4YWEtY2M2OC00MTNhLTgwMzQtYzI3MTZhMmNlMTRhIn19.BrCE3O2ZoOllnX_ee5gxOynzxvZQLBZA5c9nQqP0EO8mSym3GLGU4wb_asJba1BshZT78jaTxEeIbttsxPN_-o_MMmDw41kNAvLnYxbESr9K4kXLY64UUUAGxGQt0szzZStNZXjj_a3ze5VReiE6zSg59apox-fgOFnepUhBW-dv7ah1STMw-4bvE-0JvqD0Fss_9_Yx7s5ElVrzpSJPV2dMaGcUh_A7eWxa_DdDBvQOJ7fXaQ8_jGsWxtcpFDCK1iW6pGVJAQL_5kWTAsP_Qx_JHr0UYI8FokyDXuZ7qJXRQcK-UQdbwy6PNqL-wKi1xe5s74iY4OOKsXfAiSch4AbTIa6JTRJXkegx78vZ0GzFIj5SntszY6kQ5PjPmjTm4P35hVWIKhoFAKPOpt23MjaD0g2PkSQRD8sVNhO0AKSA4Z1k-0h6ec94FaA9iR1Kz0bpdgzV6vZB702gcijm-fxLp0_xDTRhFJffOWrNP7JAA3MpFZMdsps3HHMTfc2TVG1w6BBdCw-pGHqyUOaId54riFskhK__4JLB4uRDnKy0Gn_liiHHCrYSbYYWuGv9ZLh0zwA1m8pBi8IlXd0YC03RLtRY0AOdeN9Km1lvCCrmzm8ZrmJlthk30wlud4KbJlOzogkgq2ULhU0gLFaujguHuiBrEYue64R-lDCBh-E'
	// 'https://my.exnessaffiliates.com/api/reports/clients/?client_account=117057472'

	@GetMapping("/exaffiliates/{day}")
	public ResponseEntity<String> retrieveData(@PathVariable("day") int day)
			throws JsonMappingException, JsonProcessingException {
		Date currentDateTime = new Date();

		// Lấy ngày hiện tại
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(currentDateTime);

		// Đặt thời gian thành 00:00:01
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 1);

		// Lấy timestamp sau khi đặt thời gian
		long timestamp = calendar.getTimeInMillis() / 1000 - (86400 * day);

		// Tạo đối tượng SimpleDateFormat với định dạng "yyyy-MM-dd"
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		// Chuyển đổi timestamp thành đối tượng Date
		Date date = new Date(timestamp * 1000); // *1000 để đổi về milliseconds

		// Chuyển đối tượng Date thành chuỗi với định dạng "yyyy-MM-dd"
		String formattedDate = dateFormat.format(date);

		String url = "https://my.exnessaffiliates.com/api/v2/auth/";

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("Accept", "application/json");

		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setLogin("Long_phan@ymail.com");
		loginRequest.setPassword("Xitrum11");

		HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

		ResponseEntity<AuthResponse> responseEntity = new RestTemplate().exchange(url, HttpMethod.POST, request,
				AuthResponse.class);
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			AuthResponse authResponse = responseEntity.getBody();
			String token = authResponse.getToken();

			// Gọi API khác với token
			// Ví dụ: Gửi yêu cầu GET đến một API sử dụng token
			String apiUrl = "https://my.exaffiliates.com/api/reports/rewards/?reward_date_from=" + formattedDate
					+ "&reward_date_to=" + formattedDate;

			HttpHeaders headersWithToken = new HttpHeaders();
			headersWithToken.set("Authorization", "JWT " + token);

			HttpEntity<String> requestWithToken = new HttpEntity<>(headersWithToken);

			ResponseEntity<String> apiResponse = new RestTemplate().exchange(apiUrl, HttpMethod.GET, requestWithToken,
					String.class);
			List<DataItem> dataItems = new ArrayList<>();
			String json = apiResponse.getBody();

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(json); // Chuyển JSON thành một đối tượng JsonNode

			if (rootNode.has("data")) {
				JsonNode dataNode = rootNode.get("data");
				if (dataNode.isArray()) {
					dataItems = objectMapper.readValue(dataNode.toString(), new TypeReference<List<DataItem>>() {
					});
				}
			}

			System.out.println(dataItems);

			double[] totalAmount = { 0.0, 0.0, 0.0 };

			List<History> toAdmin = new ArrayList<>();
			List<History> toUser = new ArrayList<>();
			List<History> toLisa = new ArrayList<>();
			List<String> listExness = new ArrayList<>();
			StringBuilder sb = new StringBuilder();

			for (DataItem item : dataItems) {
				Long clientAccount = item.getClient_account();
				Optional<Exness> exness = exService.findByExnessId(String.valueOf(clientAccount));

				if (exness.isEmpty()) {
					continue;
				}
				if (exness.get().getUser().getBranchName().equals("ALEX")) {
					Double rewardUsd = Double.parseDouble(item.getReward_usd());

					Commission commission = new Commission();
					commission.setAmount(rewardUsd);
					commission.setExnessId(exness.get().getExness());
					commission.setTransactionId(item.getClient_uid());
					commission.setTime(timestamp);
					try {
						commissService.saveCommission(commission);
					} catch (Exception e) {
						e.printStackTrace();
					}

					userService.updateTotalCommission(exness.get().getUser(), rewardUsd);
				} else if (exness.get().getUser().getBranchName().equals("LISA")) {
					long exnessTransaction = item.getId();
					long exnessId = item.getClient_account();
					double amount = Double.parseDouble(item.getReward_usd());
					double originalAmountPayToNetwork = amount * 0.8;
					double remainingAmountPayToNetwork = originalAmountPayToNetwork;
					double amountToAdmin = amount - originalAmountPayToNetwork;
					// Kiem tra khoan hoa hong do da tra hay chua
					Optional<ExnessTransaction> exTran = exTranRepo
							.findByTransactionExness(String.valueOf(exnessTransaction));
					if (exTran.isPresent()) {
						sb.append(exnessTransaction + " đã được chi trả.\n");
						continue;
					} else {
						totalAmount[0] += amount;
						totalAmount[1] += amountToAdmin;
						// Chi cho system 20% IB
						History historyToAdmin = new History();
						User userAdmin = userRepo.getByEmail("admin@gmail.com");
						historyToAdmin.setAmount(amountToAdmin);
						historyToAdmin.setReceiver(userAdmin.getEmail());
						historyToAdmin.setSender(String.valueOf(exnessId));
						historyToAdmin.setTransaction(String.valueOf(exnessTransaction));
						historyToAdmin.setTime(String.valueOf(timestamp));
						historyToAdmin.setMessage("20% từ số IB=" + amount + " của ExnessID=" + exnessId);
						toAdmin.add(historyToAdmin);

						HashMap<Integer, String> listToPayIB = getNetWorkToLisa(String.valueOf(exnessId));
						for (HashMap.Entry<Integer, String> entry : listToPayIB.entrySet()) {
							String recipientEmail = entry.getValue();
							double amountToPay = 0.0;

							if (recipientEmail.equals("lisa@gmail.com")) {
								// Nếu người nhận là lisa@gmail, gửi toàn bộ số remainingAmountPayToNetwork (số
								// IB chia còn lại khi gặp lisa@gmail.com) cho họ
								amountToPay = remainingAmountPayToNetwork;
								History historyToLisa = new History();
								User userLisa = userRepo.findByEmail("lisa@gmail.com").get();
								historyToLisa.setAmount(amountToPay);
								historyToLisa.setReceiver(userLisa.getEmail());
								historyToLisa.setSender(String.valueOf(exnessId));
								historyToLisa.setTransaction(String.valueOf(exnessTransaction));
								historyToLisa.setTime(String.valueOf(timestamp));
								historyToLisa.setMessage(
										"Tìm thấy Lisa, chi hết số IB=" + amount + " còn lại của ExnessID=" + exnessId);

								toLisa.add(historyToLisa);

								totalAmount[2] += amountToPay;
								remainingAmountPayToNetwork -= amountToPay;
								break; // Dừng vòng lặp vì đã gửi hết số tiền
							} else {
								if (recipientEmail.equals("admin@gmail.com")) {
									// Không chia cho tài khoản
									continue;
								} else {
									// Ngược lại, gửi 50% của remainingAmountPayToNetwork cho người nhận
									amountToPay = remainingAmountPayToNetwork / 2;
									History historyToSystem = new History();
									Optional<User> userTemp = userRepo.findByEmail(recipientEmail);
									if (userTemp.isEmpty()) {
										continue;
									}

									User userInSystem = userTemp.get();
									double amountOfUser = exService.getBalanceByEmail(userInSystem.getEmail());
									if (amountOfUser < 100_000) {
										break;
									}
									historyToSystem.setAmount(amountToPay);
									historyToSystem.setReceiver(userInSystem.getEmail());
									historyToSystem.setSender(String.valueOf(exnessId));
									historyToSystem.setTransaction(String.valueOf(exnessTransaction));
									historyToSystem.setTime(String.valueOf(timestamp));
									historyToSystem
											.setMessage("Hoa hồng từ khoản IB=" + amount + " của ExnessID=" + exnessId);
									toUser.add(historyToSystem);

									totalAmount[2] += amountToPay;
									remainingAmountPayToNetwork -= amountToPay; // Giảm số tiền còn lại
								}
							}
						}
						if (remainingAmountPayToNetwork > 0) {
							History historyToLisa = new History();
							User userLisa = userRepo.findByEmail("lisa@gmail.com").get();
							historyToLisa.setAmount(remainingAmountPayToNetwork);
							historyToLisa.setReceiver(userLisa.getEmail());
							historyToLisa.setSender(String.valueOf(exnessId));
							historyToLisa.setTransaction(String.valueOf(exnessTransaction));
							historyToLisa.setTime(String.valueOf(timestamp));
							historyToLisa.setMessage("Số còn lại từ khoản IB=" + amount + " của ExnessID=" + exnessId);

							toLisa.add(historyToLisa);
						}

						listExness.add(String.valueOf(exnessTransaction));
					}
				}

			}

			Thread thread1 = new Thread() {

				public void run() {
					for (String item : listExness) {
						ExnessTransaction exnessTransactionFromExcel = new ExnessTransaction();
						exnessTransactionFromExcel.setTime(String.valueOf(System.currentTimeMillis()));
						exnessTransactionFromExcel.setTransactionExness(item);
						exTranRepo.save(exnessTransactionFromExcel);
					}
				}
			};

			Thread thread2 = new Thread() {
				public void run() {
					for (History item : toAdmin) {
						hisService.saveHistory(item);
						User user = userRepo.findByEmail(item.getReceiver()).get();
						user.setCommission(user.getCommission() + item.getAmount());
						userRepo.save(user);

					}
				}
			};

			Thread thread3 = new Thread() {
				public void run() {
					for (History item : toLisa) {
						hisService.saveHistory(item);
						User user = userRepo.findByEmail(item.getReceiver()).get();
						user.setCommission(user.getCommission() + item.getAmount());
						userRepo.save(user);
					}
				}
			};

			Thread thread4 = new Thread() {
				public void run() {
					for (History item : toUser) {
						hisService.saveHistory(item);
						User user = userRepo.findByEmail(item.getReceiver()).get();
						user.setCommission(user.getCommission() + item.getAmount());
						userRepo.save(user);
					}
				}
			};

			thread1.start();
			thread2.start();
			thread3.start();
			thread4.start();
		}

		return ResponseEntity.ok("OK");
	}

	@GetMapping("/getIB/{day}")
	public ResponseEntity<JsonNode> getIB(@PathVariable("day") String day)
			throws JsonMappingException, JsonProcessingException {
		JsonNode rootNode = null;
		String url = "https://my.exnessaffiliates.com/api/v2/auth/";

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("Accept", "application/json");

		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setLogin("Long_phan@ymail.com");
		loginRequest.setPassword("Xitrum11");

		HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

		ResponseEntity<AuthResponse> responseEntity = new RestTemplate().exchange(url, HttpMethod.POST, request,
				AuthResponse.class);
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			AuthResponse authResponse = responseEntity.getBody();
			String token = authResponse.getToken();

			// Gọi API khác với token
			// Ví dụ: Gửi yêu cầu GET đến một API sử dụng token
			String apiUrl = "https://my.exaffiliates.com/api/reports/rewards/?reward_date_from=" + day
					+ "&reward_date_to=" + day;

			HttpHeaders headersWithToken = new HttpHeaders();
			headersWithToken.set("Authorization", "JWT " + token);

			HttpEntity<String> requestWithToken = new HttpEntity<>(headersWithToken);

			ResponseEntity<String> apiResponse = new RestTemplate().exchange(apiUrl, HttpMethod.GET, requestWithToken,
					String.class);
			String json = apiResponse.getBody();

			ObjectMapper objectMapper = new ObjectMapper();
			rootNode = objectMapper.readTree(json); // Chuyển JSON thành một đối tượng JsonNode
		}

		return ResponseEntity.ok(rootNode);
	}

	@GetMapping("/share-ib")
//	@Scheduled(cron = "0 5 7 * * *", zone="GMT+7:00")
	public ResponseEntity<List<DataItem>> shareIb() throws JsonMappingException, JsonProcessingException {
		Date currentDateTime = new Date();

		// Lấy ngày hiện tại
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(currentDateTime);

		// Đặt thời gian thành 00:00:01
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 1);

		// Lấy timestamp sau khi đặt thời gian
		long timestamp = calendar.getTimeInMillis() / 1000 - 86400;

		// Tạo đối tượng SimpleDateFormat với định dạng "yyyy-MM-dd"
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		// Chuyển đổi timestamp thành đối tượng Date
		Date date = new Date(timestamp * 1000); // *1000 để đổi về milliseconds

		// Chuyển đối tượng Date thành chuỗi với định dạng "yyyy-MM-dd"
		String formattedDate = dateFormat.format(date);

		String url = "https://my.exnessaffiliates.com/api/v2/auth/";

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("Accept", "application/json");

		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setLogin("Long_phan@ymail.com");
		loginRequest.setPassword("Xitrum11");
		List<DataItem> dataItems = new ArrayList<>();

		HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

		ResponseEntity<AuthResponse> responseEntity = new RestTemplate().exchange(url, HttpMethod.POST, request,
				AuthResponse.class);
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			AuthResponse authResponse = responseEntity.getBody();
			String token = authResponse.getToken();

			// Gọi API khác với token
			// Ví dụ: Gửi yêu cầu GET đến một API sử dụng token
			String apiUrl = "https://my.exaffiliates.com/api/reports/rewards/?reward_date_from=" + formattedDate
					+ "&reward_date_to=" + formattedDate;

			HttpHeaders headersWithToken = new HttpHeaders();
			headersWithToken.set("Authorization", "JWT " + token);

			HttpEntity<String> requestWithToken = new HttpEntity<>(headersWithToken);

			ResponseEntity<String> apiResponse = new RestTemplate().exchange(apiUrl, HttpMethod.GET, requestWithToken,
					String.class);

			String json = apiResponse.getBody();

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(json); // Chuyển JSON thành một đối tượng JsonNode

			if (rootNode.has("data")) {
				JsonNode dataNode = rootNode.get("data");
				if (dataNode.isArray()) {
					dataItems = objectMapper.readValue(dataNode.toString(), new TypeReference<List<DataItem>>() {
					});
				}
			}
		}

		double[] totalAmount = { 0.0, 0.0, 0.0 };

		List<History> toAdmin = new ArrayList<>();
		List<History> toUser = new ArrayList<>();
		List<History> toLisa = new ArrayList<>();
		List<String> listExness = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		for (DataItem item : dataItems) {
			long exnessTransaction = item.getId();
			long exnessId = item.getClient_account();
			double amount = Double.parseDouble(item.getReward_usd());
			double originalAmountPayToNetwork = amount * 0.8;
			double remainingAmountPayToNetwork = originalAmountPayToNetwork;
			double amountToAdmin = amount - originalAmountPayToNetwork;
			// Kiem tra khoan hoa hong do da tra hay chua
			Optional<ExnessTransaction> exTran = exTranRepo.findByTransactionExness(String.valueOf(exnessTransaction));
			if (exTran.isPresent()) {
				sb.append(exnessTransaction + " đã được chi trả.\n");
				continue;
			} else {
				totalAmount[0] += amount;
				totalAmount[1] += amountToAdmin;
				// Chi cho system 20% IB
				History historyToAdmin = new History();
				User userAdmin = userRepo.getByEmail("admin@gmail.com");
				historyToAdmin.setAmount(amountToAdmin);
				historyToAdmin.setReceiver(userAdmin.getEmail());
				historyToAdmin.setSender(String.valueOf(exnessId));
				historyToAdmin.setTransaction(String.valueOf(exnessTransaction));
				historyToAdmin.setTime(String.valueOf(System.currentTimeMillis() / 1000));
				historyToAdmin.setMessage("20% từ số IB=" + amount + " của ExnessID=" + exnessId);
				toAdmin.add(historyToAdmin);

				HashMap<Integer, String> listToPayIB = getNetWorkToLisa(String.valueOf(exnessId));
				for (HashMap.Entry<Integer, String> entry : listToPayIB.entrySet()) {
					String recipientEmail = entry.getValue();
					double amountToPay = 0.0;

					if (recipientEmail.equals("lisa@gmail.com")) {
						// Nếu người nhận là lisa@gmail, gửi toàn bộ số remainingAmountPayToNetwork (số
						// IB chia còn lại khi gặp lisa@gmail.com) cho họ
						amountToPay = remainingAmountPayToNetwork;
						History historyToLisa = new History();
						User userLisa = userRepo.findByEmail("lisa@gmail.com").get();
						historyToLisa.setAmount(amountToPay);
						historyToLisa.setReceiver(userLisa.getEmail());
						historyToLisa.setSender(String.valueOf(exnessId));
						historyToLisa.setTransaction(String.valueOf(exnessTransaction));
						historyToLisa.setTime(String.valueOf(System.currentTimeMillis() / 1000));
						historyToLisa.setMessage(
								"Tìm thấy Lisa, chi hết số IB=" + amount + " còn lại của ExnessID=" + exnessId);

						toLisa.add(historyToLisa);

						totalAmount[2] += amountToPay;
						remainingAmountPayToNetwork -= amountToPay;
						break; // Dừng vòng lặp vì đã gửi hết số tiền
					} else {
						if (recipientEmail.equals("admin@gmail.com")) {
							// Không chia cho tài khoản
							continue;
						} else {
							// Ngược lại, gửi 50% của remainingAmountPayToNetwork cho người nhận
							amountToPay = remainingAmountPayToNetwork / 2;
							History historyToSystem = new History();
							Optional<User> userTemp = userRepo.findByEmail(recipientEmail);
							if (userTemp.isEmpty()) {
								continue;
							}

							User userInSystem = userTemp.get();
							double amountOfUser = exService.getBalanceByEmail(userInSystem.getEmail());
							if (amountOfUser < 100_000) {
								break;
							}
							historyToSystem.setAmount(amountToPay);
							historyToSystem.setReceiver(userInSystem.getEmail());
							historyToSystem.setSender(String.valueOf(exnessId));
							historyToSystem.setTransaction(String.valueOf(exnessTransaction));
							historyToSystem.setTime(String.valueOf(System.currentTimeMillis() / 1000));
							historyToSystem.setMessage("Hoa hồng từ khoản IB=" + amount + " của ExnessID=" + exnessId);
							toUser.add(historyToSystem);

							totalAmount[2] += amountToPay;
							remainingAmountPayToNetwork -= amountToPay; // Giảm số tiền còn lại
						}
					}
				}
				if (remainingAmountPayToNetwork > 0) {
					History historyToLisa = new History();
					User userLisa = userRepo.findByEmail("lisa@gmail.com").get();
					historyToLisa.setAmount(remainingAmountPayToNetwork);
					historyToLisa.setReceiver(userLisa.getEmail());
					historyToLisa.setSender(String.valueOf(exnessId));
					historyToLisa.setTransaction(String.valueOf(exnessTransaction));
					historyToLisa.setTime(String.valueOf(System.currentTimeMillis() / 1000));
					historyToLisa.setMessage("Số còn lại từ khoản IB=" + amount + " của ExnessID=" + exnessId);

					toLisa.add(historyToLisa);
				}

				listExness.add(String.valueOf(exnessTransaction));
			}
		}

		Thread thread1 = new Thread() {

			public void run() {
				for (String item : listExness) {
					ExnessTransaction exnessTransactionFromExcel = new ExnessTransaction();
					exnessTransactionFromExcel.setTime(String.valueOf(System.currentTimeMillis()));
					exnessTransactionFromExcel.setTransactionExness(item);
					exTranRepo.save(exnessTransactionFromExcel);
				}
			}
		};

		Thread thread2 = new Thread() {
			public void run() {
				for (History item : toAdmin) {
					hisService.saveHistory(item);
					User user = userRepo.findByEmail(item.getReceiver()).get();
					user.setCommission(user.getCommission() + item.getAmount());
					userRepo.save(user);

				}
			}
		};

		Thread thread3 = new Thread() {
			public void run() {
				for (History item : toLisa) {
					hisService.saveHistory(item);
					User user = userRepo.findByEmail(item.getReceiver()).get();
					user.setCommission(user.getCommission() + item.getAmount());
					userRepo.save(user);
				}
			}
		};

		Thread thread4 = new Thread() {
			public void run() {
				for (History item : toUser) {
					hisService.saveHistory(item);
					User user = userRepo.findByEmail(item.getReceiver()).get();
					user.setCommission(user.getCommission() + item.getAmount());
					userRepo.save(user);
				}
			}
		};

		thread1.start();
		thread2.start();
		thread3.start();
		thread4.start();

		System.out.println(sb.toString());
		return ResponseEntity.ok(dataItems);
	}

	@GetMapping("/transfer-transaction/exnessId={exnessId}&transaction={type}&amount={amount}&time={time}")
	public ResponseEntity<String> insertData(@PathVariable("exnessId") String exnessId, @PathVariable("type") int type,
			@PathVariable("amount") double amount, @PathVariable("time") long time) {
		Optional<Exness> exness = exRepo.findByExness(exnessId);
		if (exness.isEmpty()) {
			throw new NotFoundException("This exness " + exnessId + " is not existed!");
		}

		String transactionType;
		if (type == 0) {
			transactionType = "Withdraw";
		} else {
			transactionType = "Deposit";
		}

		try {
			Transaction transaction = new Transaction();
			transaction.setExnessId(exnessId);
			transaction.setAmount(Math.abs(amount));
			transaction.setType(transactionType);
			transaction.setTime(time);
			tranService.saveTransaction(transaction);

		} catch (Exception e) {
			throw new ExistedException("Exness ID " + exnessId + " has already saved");
		}

		return ResponseEntity.ok("OK");
	}

	@GetMapping("/totalHistory")
	public ResponseEntity<String> getTotalDataSent() {
		Date currentDate = new Date();

		// Lấy ngày hiện tại
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(currentDate);

		// Đặt thời gian thành 00:00:01
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 1);

		String msg = "";
		// Lấy timestamp sau khi đặt thời gian
		long timestamp = calendar.getTimeInMillis() / 1000 - 86400;
		List<Profit> profits = proService.findAmountOfProfitsByTime(timestamp);
		msg += ">> Profits " + profits.size() + "\n";
		for (Profit profit : profits) {
			if (profit.getAmount() == 0) {
				msg += ">> ID " + profit.getExnessId() + " = 0\n";
			}
		}
		List<Balance> balances = balanceService.findAmountOfBalanceByTime(timestamp);
		msg += ">> Balances " + balances.size();
		for (Balance balance : balances) {
			if (balance.getAmount() == 0) {
				msg += ">> ID " + balance.getExnessId() + " = 0\n";
			}
		}

		List<Exness> listExness = exService.findAllExness();
		for (Exness exness : listExness) {
			double sumAmountProfit = proService.sumTotalProfit(exness.getExness());
			exService.fixTotalProfit(exness.getExness(), sumAmountProfit);
		}

		return ResponseEntity.ok(msg);
	}

	@GetMapping("/transfer-data/exnessId={exnessId}&balance={balance}&profit={profit}")
	public ResponseEntity<String> insertData(@PathVariable("exnessId") String exnessId,
			@PathVariable("balance") double balance, @PathVariable("profit") double profit) {
		Optional<Exness> exness = exRepo.findByExness(exnessId);
		if (exness.isEmpty()) {
			throw new NotFoundException("This exness " + exnessId + " is not existed!");
		}

		Date currentDate = new Date();

		// Lấy ngày hiện tại
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(currentDate);

		// Đặt thời gian thành 00:00:01
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 1);

		// Lấy timestamp sau khi đặt thời gian
		long timestamp = calendar.getTimeInMillis() / 1000 - 86400;

		// 1) Lưu profit của ngày trước đó
		boolean checkProfit = userService.saveProfit(exnessId, profit, timestamp);
		// 2) Lưu balance của ngày trước đó
		boolean checkBalance = userService.saveBalance(exnessId, balance, timestamp);

		if (checkProfit && checkBalance) {
			userService.updateBalanceExness(exnessId, balance);
			// 4) Cập nhật tổng profit
			exService.updateTotalProfit(exnessId, profit);
		} else {
			throw new ExistedException("Exness ID " + exnessId + " on " + calendar.getTime() + " has already saved");
		}

		return ResponseEntity.ok(String.valueOf(timestamp));
	}
	
	@GetMapping("/transfer-data/date={date}/exnessId={exnessId}&balance={balance}&profit={profit}")
	public ResponseEntity<String> insertDataByDate(@PathVariable("exnessId") String exnessId,
			@PathVariable("balance") double balance, @PathVariable("profit") double profit, @PathVariable("date") int date) {
		Optional<Exness> exness = exRepo.findByExness(exnessId);
		if (exness.isEmpty()) {
			throw new NotFoundException("This exness " + exnessId + " is not existed!");
		}

		Date currentDate = new Date();

		// Lấy ngày hiện tại
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(currentDate);

		// Đặt thời gian thành 00:00:01
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 1);

		// Lấy timestamp sau khi đặt thời gian
		long timestamp = calendar.getTimeInMillis() / 1000 - (86400*date);

		// 1) Lưu profit của ngày trước đó
		boolean checkProfit = userService.saveProfit(exnessId, profit, timestamp);
		// 2) Lưu balance của ngày trước đó
		boolean checkBalance = userService.saveBalance(exnessId, balance, timestamp);

		if (checkProfit && checkBalance) {
			userService.updateBalanceExness(exnessId, balance);
			// 4) Cập nhật tổng profit
			exService.updateTotalProfit(exnessId, profit);
		} else {
			throw new ExistedException("Exness ID " + exnessId + " on " + calendar.getTime() + " has already saved");
		}

		return ResponseEntity.ok(String.valueOf(timestamp));
	}

	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
		return ResponseEntity.ok(service.register(request));
	}

	@PostMapping("/registerLisa")
	public ResponseEntity<AuthenticationResponse> registerLisa(@RequestBody RegisterLisaRequest request) {
		return ResponseEntity.ok(service.registerLisa(request));
	}

	@PostMapping("/authenticate")
	public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
		return ResponseEntity.ok(service.authenticate(request));
	}

	@PostMapping("/authenticateLisa")
	public ResponseEntity<AuthenticationResponse> authenticateLisa(@RequestBody AuthenticationRequest request) {
		return ResponseEntity.ok(service.authenticateLisa(request));
	}

	@PostMapping("/getCode")
	public ResponseEntity<String> getCode(@RequestBody RefferalRequest request) {
		return ResponseEntity.ok(service.generateCode(request.getEmail()));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
		return ResponseEntity.ok(service.forgotPassword(request));
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout(@RequestBody LogoutRequest request) {
		return ResponseEntity.ok(service.logout(request.getAccess_token()));
	}

	@PostMapping("/refresh-token")
	public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		service.refreshToken(request, response);
	}

	private HashMap<Integer, String> getNetWorkToLisa(String exness) {
		HashMap<Integer, String> listNetWorks = new HashMap<>();
		try {
			Optional<Exness> exnessF0 = exRepo.findByExness(exness);
			int level = 1;

			String userF1 = exnessF0.get().getUser().getRefferal();
			listNetWorks.put(level, userF1);
			level++;
			String userPointer = userF1;

			do {
				Optional<User> user = userRepo.findByEmail(userPointer);
				if (user.isEmpty()) {
					break;
				}
				if (!user.get().getRefferal().equals("")) {
					listNetWorks.put(level, user.get().getRefferal());
				}

				userPointer = user.get().getRefferal();
				level++;
			} while (!userPointer.equals("lisa@gmail.com") && level <= 5);
		} catch (Exception e) {
			return new HashMap<>();
		}

		return listNetWorks;
	}
}
