package com.web.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.config.JwtService;
import com.web.dto.EmailDto;
import com.web.dto.ExnessInfoDto;
import com.web.dto.ExnessResponse;
import com.web.dto.UpdateInfoRequest;
import com.web.exception.ExistedException;
import com.web.exception.NotFoundException;
import com.web.service.MailService;
import com.web.service.PrevService;
import com.web.token.Token;
import com.web.token.TokenRepository;
import com.web.token.TokenType;
import com.web.user.Confirmation;
import com.web.user.ConfirmationRepository;
import com.web.user.Exness;
import com.web.user.ExnessRepository;
import com.web.user.Role;
import com.web.user.User;
import com.web.user.UserRepository;

import dev.samstevens.totp.secret.SecretGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
	private final UserRepository repository;
	private final ExnessRepository exRepo;
	private final TokenRepository tokenRepository;
	private final PrevService prevService;
	private final PasswordEncoder passwordEncoder;
	private final MailService mailService;
	private final SecretGenerator secretGenerator;
	private final ConfirmationRepository conRepo;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public void activeExness(String exness) {
		Exness item = exRepo.findByExness(exness).get();
		item.setActive(true);
		item.setMessage("");
		exRepo.save(item);

		String message = "Exness ID: " + exness + " đã được active!";
	}

	public List<ExnessResponse> getAllExness(String email) {
		User user = repository.getByEmail(email);
		List<Exness> exnesses = exRepo.findByUser(user);
		List<ExnessResponse> results = new ArrayList<>();

		for (Exness exness : exnesses) {
			if (exness.getUser().getBranchName().equals("ALEX")) {
				ExnessResponse response = new ExnessResponse();
				response.setExnessId(exness.getExness());
				response.setServer(exness.getServer());
				response.setPassword(exness.getPassword());
				response.setStatus(exness.isActive());
				response.setMessage(exness.getMessage());
				results.add(response);
			}
		}

		return results;
	}

	public List<ExnessResponse> getAllExnessLisa(String email) {
		User user = repository.getByEmail(email);
		List<Exness> exnesses = exRepo.findByUser(user);
		List<ExnessResponse> results = new ArrayList<>();

		for (Exness exness : exnesses) {
			ExnessResponse response = new ExnessResponse();
			response.setExnessId(exness.getExness());
			response.setServer(exness.getServer());
			response.setPassword(exness.getPassword());
			response.setStatus(exness.isActive());
			response.setMessage(exness.getMessage());
			response.setReason(exness.getReason());
			results.add(response);
		}

		return results;
	}

	@Transactional
	public AuthenticationResponse register(RegisterRequest request) {
		Optional<User> userByEmail = repository.findByEmail(request.getEmail());
		if (userByEmail.isPresent()) {
			throw new ExistedException("Tài khoản này đã tồn tại");
		}

		String codeRef = generateRandomNumberString(10);

		Optional<User> userByCode = repository.findByCode(codeRef);
		if (userByCode.isPresent()) {
			throw new ExistedException("Mã giới thiệu này đã tồn tại!");
		}

		Optional<User> userByRefCode = repository.findByCode(request.getRefferal());
		if (userByRefCode.isEmpty()) {
			throw new NotFoundException("Mã giới thiệu này không tồn tại!");
		}

		String secret = secretGenerator.generate();

		var user = User.builder().firstname(request.getFirstname()).lastname(request.getLastname())
				.email(request.getEmail()).password(passwordEncoder.encode(request.getPassword())).role(Role.USER)
				.refferal(userByRefCode.get().getEmail()).exnessList(new ArrayList<>()).code(codeRef).branchName("ALEX")
				.secret(secret).build();

		var savedUser = repository.save(user);
		var jwtToken = jwtService.generateToken(user);
		var refreshToken = jwtService.generateRefreshToken(user);
		saveUserToken(savedUser, jwtToken);

		// prevService.initPrev(request.getEmail());
		return AuthenticationResponse.builder().accessToken(jwtToken).refreshToken(refreshToken).build();
	}

	@Transactional
	public AuthenticationResponse registerLisa(RegisterLisaRequest request) {
		Optional<User> userByEmail = repository.findByEmail(request.getEmail());
		if (userByEmail.isPresent()) {
			throw new ExistedException("Tài khoản này đã tồn tại");
		}

		String codeRef = generateRandomNumberString(10);

		Optional<User> userByRefCode = repository.findByCode(request.getRefferal());
		if (userByRefCode.isEmpty()) {
			throw new NotFoundException("Mã giới thiệu này không tồn tại!");
		}

		String secret = secretGenerator.generate();

		var user = User.builder().firstname(request.getFirstname()).lastname(request.getLastname())
				.email(request.getEmail()).password(passwordEncoder.encode(request.getPassword())).role(Role.USER)
				.refferal(userByRefCode.get().getEmail()).exnessList(new ArrayList<>()).code(codeRef)
				.branchName(request.getBranchName()).secret(secret).build();

		var savedUser = repository.save(user);
		var jwtToken = jwtService.generateToken(user);
		var refreshToken = jwtService.generateRefreshToken(user);
		saveUserToken(savedUser, jwtToken);

		// prevService.initPrev(request.getEmail());
		return AuthenticationResponse.builder().accessToken(jwtToken).refreshToken(refreshToken).build();
	}

	public void editInfo(UpdateInfoRequest request) {
		User user = repository.findByEmail(request.getEmail()).get();
		user.setFirstname(request.getFirstName());
		user.setLastname(request.getLastName());
		user.setBio(request.getBio());
		repository.save(user);
	}

	public List<String> getExnessByEmail(String email) {
		if (email.equalsIgnoreCase("all")) {
			List<Exness> listExness = exRepo.findAll();
			List<String> results = new ArrayList<>();
			for (Exness exness : listExness) {
				if (exness.getUser().getBranchName().equalsIgnoreCase("PixiuGroup")) {
					results.add(exness.getExness());
				}
			}

			return results;
		} else if (email.indexOf("m-") != -1) {
			Optional<User> user = repository.findByEmail(email.substring(2, email.length()));
			if (user.isEmpty()) {
				throw new NotFoundException("Địa chỉ email không tồn tại!");
			}

			List<String> results = new ArrayList<>();
			for (Exness exness : user.get().getExnessList()) {
				results.add(exness.getExness());
			}

			findUserByReferral(user.get().getEmail(), results);

			return results;
		} else {
			Optional<User> user = repository.findByEmail(email);
			if (user.isEmpty()) {
				throw new NotFoundException("Địa chỉ email không tồn tại!");
			}
			List<Exness> listExness = exRepo.findByUser(user.get());

			List<String> results = new ArrayList<>();
			for (Exness ex : listExness) {
				results.add(ex.getExness());
			}

			return results;
		}
	}

	public List<ExnessInfoDto> getExnessByEmailPixiu(String email) {
		if (email.equalsIgnoreCase("all")) {
			List<Exness> listExness = exRepo.findAll();
			List<ExnessInfoDto> results = new ArrayList<>();
			for (Exness exness : listExness) {
				if (exness.getUser().getBranchName().equalsIgnoreCase("PixiuGroup")) {
					ExnessInfoDto item = new ExnessInfoDto();
					item.setExnessId(exness.getExness());
					item.setFullname(exness.getUser().getFirstname() + " " + exness.getUser().getLastname());
					results.add(item);
				}
			}

			return results;
		} else if (email.indexOf("m-") != -1) {
			Optional<User> user = repository.findByEmail(email.substring(2, email.length()));
			if (user.isEmpty()) {
				throw new NotFoundException("Địa chỉ email không tồn tại!");
			}

			List<ExnessInfoDto> results = new ArrayList<>();
			for (Exness exness : user.get().getExnessList()) {
				ExnessInfoDto item = new ExnessInfoDto();
				item.setExnessId(exness.getExness());
				item.setFullname(exness.getUser().getFirstname() + " " + exness.getUser().getLastname());
				results.add(item);
			}

			findUserByReferralPixiu(user.get().getEmail(), results);

			return results;
		} else {
			Optional<User> user = repository.findByEmail(email);
			if (user.isEmpty()) {
				throw new NotFoundException("Địa chỉ email không tồn tại!");
			}
			List<Exness> listExness = exRepo.findByUser(user.get());

			List<ExnessInfoDto> results = new ArrayList<>();
			for (Exness exness : listExness) {
				ExnessInfoDto item = new ExnessInfoDto();
				item.setExnessId(exness.getExness());
				item.setFullname(exness.getUser().getFirstname() + " " + exness.getUser().getLastname());
				results.add(item);
			}

			return results;
		}

	}

	private void findUserByReferral(String email, List<String> results) {
		// Tìm kiếm theo chiều sâu cho người được giới thiệu
		List<User> users = repository.findByRefferal(email);

		for (User user : users) {
			for (Exness exness : user.getExnessList()) {
				results.add(exness.getExness());
			}

			findUserByReferral(user.getEmail(), results); // Gọi đệ quy để tìm kiếm theo chiều sâu
		}
	}

	private void findUserByReferralPixiu(String email, List<ExnessInfoDto> results) {
		// Tìm kiếm theo chiều sâu cho người được giới thiệu
		List<User> users = repository.findByRefferal(email);

		for (User user : users) {
			for (Exness exness : user.getExnessList()) {
				ExnessInfoDto item = new ExnessInfoDto();
				item.setExnessId(exness.getExness());
				item.setFullname(exness.getUser().getFirstname() + " " + exness.getUser().getLastname());
				results.add(item);
			}

			findUserByReferralPixiu(user.getEmail(), results); // Gọi đệ quy để tìm kiếm theo chiều sâu
		}
	}

	public AuthenticationResponse authenticate(AuthenticationRequest request) {
		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
		var user = repository.findByEmailAlex(request.getEmail()).orElseThrow();
		var jwtToken = jwtService.generateToken(user);
		var refreshToken = jwtService.generateRefreshToken(user);
		revokeAllUserTokens(user);
		saveUserToken(user, jwtToken);
		return AuthenticationResponse.builder().accessToken(jwtToken).refreshToken(refreshToken).build();
	}

	public AuthenticationResponse authenticateLisa(AuthenticationRequest request) {
		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
		var user = repository.findByEmail(request.getEmail()).orElseThrow();
		var jwtToken = jwtService.generateToken(user);
		var refreshToken = jwtService.generateRefreshToken(user);
		var role = "";
		if (user.getRole().name().equalsIgnoreCase("admin")) {
			if (request.getEmail().equalsIgnoreCase("super_admin@gmail.com")) {
				role = "sa";
			} else {
				role = "a";
			}
		} else if (user.getRole().name().equalsIgnoreCase("manager")) {
			role = "m";
		} else if (user.getRole().name().equalsIgnoreCase("user")) {
			role = "u";
		}
		revokeAllUserTokens(user);
		saveUserToken(user, jwtToken);
		return AuthenticationResponse.builder().accessToken(jwtToken).refreshToken(refreshToken).role(role).build();
	}

	public UpdateRefResponse updateRef(String current, String code) {
		Optional<User> currentUser = repository.findByEmail(current);
		Optional<User> codeUser = repository.findByCode(code);
		if (currentUser.isEmpty()) {
			throw new NotFoundException("Tài khoản này không tồn tại!");
		} else {
			if (!currentUser.get().getRefferal().equals("")) {
				return UpdateRefResponse.builder().status(405)
						.message("Tài khoản đã liên kết thành công, không thể cập nhật!").build();
			}
			if (currentUser.get().getCode().equalsIgnoreCase(code)) {
				return UpdateRefResponse.builder().status(402).message("Không thể liên kết với chính mình!").build();
			}
		}
		if (codeUser.isEmpty()) {
			throw new NotFoundException("Mã giới thiệu không tồn tại!");
		}
		User userToUpdate = currentUser.get();
		userToUpdate.setRefferal(codeUser.get().getEmail());
		repository.save(userToUpdate);

		return UpdateRefResponse.builder().status(200).message("Liên kết thành công!").build();
	}

	private void saveUserToken(User user, String jwtToken) {
		var token = Token.builder().user(user).token(jwtToken).tokenType(TokenType.BEARER).expired(false).revoked(false)
				.build();
		tokenRepository.save(token);
	}

	@Transactional
	public UpdateRefResponse updateExness(String email, String exness, int type) {
		Optional<User> user = repository.findByEmail(email);
		if (user.isEmpty()) {
			throw new NotFoundException("Tài khoản không tồn tại.");
		}
		if (type == 1) {
			Optional<Exness> exnessToCheck = exRepo.findByExness(exness);
			if (exnessToCheck.isPresent()) {
				throw new ExistedException("Exness ID này đã tồn tại.");
			}
			User userToUpdate = user.get();
			Exness exnessToUpdate = new Exness();
			exnessToUpdate.setUser(userToUpdate);
			exnessToUpdate.setExness(exness);
			exRepo.save(exnessToUpdate);
			return UpdateRefResponse.builder().status(200).message("Exness ID cập nhật thành công cho user: " + email)
					.build();
		} else if (type == 2) {
			Optional<Exness> exnessToDelete = exRepo.findByExness(exness);
			if (exnessToDelete.isEmpty()) {
				throw new NotFoundException("Exness ID này không tồn tại.");
			}
			exRepo.delete(exnessToDelete.get());
			return UpdateRefResponse.builder().status(200).message("Exness ID xoá thành công  user: " + email).build();
		}

		return UpdateRefResponse.builder().status(405).message("Lỗi").build();
	}

	@Transactional
	public UpdateRefResponse updateExnessLisa(UpdateExnessLisaRequest request) {
		Optional<User> user = repository.findByEmail(request.getEmail());
		if (user.isEmpty()) {
			throw new NotFoundException("Tài khoản không tồn tại.");
		}
		if (request.getType() == 1) {
			Optional<Exness> exnessToCheck = exRepo.findByExness(request.getExness());
			if (exnessToCheck.isPresent()) {
				throw new ExistedException("Exness ID này đã tồn tại.");
			}
			User userToUpdate = user.get();
			Exness exnessToUpdate = new Exness();
			exnessToUpdate.setUser(userToUpdate);
			exnessToUpdate.setExness(request.getExness());
			exnessToUpdate.setServer(request.getServer());
			exnessToUpdate.setPassword(request.getPassword());
			exnessToUpdate.setLot(request.getLot());
			exnessToUpdate.setReason(request.getRate());
			if (request.getRefferal().equals("0") || request.getRefferal().equals("")) {
				exnessToUpdate.setRefferal("1");
			} else {
				exnessToUpdate.setRefferal(request.getRefferal());
			}
			
			exnessToUpdate.setName(request.getName());
			exnessToUpdate.setLatestUpdated(System.currentTimeMillis()/1000);
			exnessToUpdate.setStatus("Chưa kích hoạt");
			exnessToUpdate.setTeleId(request.getTeleId());
			exRepo.save(exnessToUpdate);
			return UpdateRefResponse.builder().status(200).message("Exness ID cập nhật thành công cho user: " + request.getEmail())
					.build();
		} else if (request.getType() == 2) {
			Optional<Exness> exnessToDelete = exRepo.findByExness(request.getExness());
			if (exnessToDelete.isEmpty()) {
				throw new NotFoundException("Exness ID này không tồn tại.");
			}
			exRepo.delete(exnessToDelete.get());
			return UpdateRefResponse.builder().status(200).message("Exness ID xoá thành công  user: " + request.getEmail()).build();
		}

		return UpdateRefResponse.builder().status(405).message("Lỗi").build();
	}

	public String logout(String accessToken) {
		Optional<Token> token = tokenRepository.findByToken(accessToken);

		if (token.isEmpty()) {
			throw new NotFoundException("Toke không tồn tại!");
		}

		token.get().setExpired(true);
		token.get().setRevoked(true);
		tokenRepository.save(token.get());
		SecurityContextHolder.clearContext();

		return "OK";
	}

	public String generateCode(String email) {
		Optional<User> user = repository.findByEmail(email);
		if (user.isEmpty()) {
			throw new NotFoundException("Email không tồn tại!");
		}
		String code = generateRandomNumberString(6);
		Confirmation confirm = new Confirmation();
		confirm.setEmail(email);
		confirm.setCode(code);
		conRepo.save(confirm);
		String content = "<body class=\"clean-body u_body\"\n"
				+ "  style=\"margin: 0;padding: 0;-webkit-text-size-adjust: 100%;background-color: #ecf0f1;color: #ffffff\">\n"
				+ "  <!--[if IE]><div class=\"ie-container\"><![endif]-->\n"
				+ "  <!--[if mso]><div class=\"mso-container\"><![endif]-->\n" + "  <table id=\"u_body\"\n"
				+ "    style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;min-width: 320px;Margin: 0 auto;background-color: #ecf0f1;width:100%\"\n"
				+ "    cellpadding=\"0\" cellspacing=\"0\">\n" + "    <tbody>\n"
				+ "      <tr style=\"vertical-align: top\">\n"
				+ "        <td style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">\n"
				+ "          <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td align=\"center\" style=\"background-color: #ecf0f1;\"><![endif]-->\n"
				+ "\n" + "\n" + "\n"
				+ "          <div class=\"u-row-container\" style=\"padding: 0px;background-color: transparent\">\n"
				+ "            <div class=\"u-row\"\n"
				+ "              style=\"margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: transparent;\">\n"
				+ "              <div\n"
				+ "                style=\"border-collapse: collapse;display: table;width: 100%;height: 100%;background-color: transparent;\">\n"
				+ "                <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-color: transparent;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px;\"><tr style=\"background-color: transparent;\"><![endif]-->\n"
				+ "\n"
				+ "                <!--[if (mso)|(IE)]><td align=\"center\" width=\"600\" style=\"background-color: #050503;width: 600px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\" valign=\"top\"><![endif]-->\n"
				+ "                <div class=\"u-col u-col-100\"\n"
				+ "                  style=\"max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;\">\n"
				+ "                  <div style=\"background-color: #050503;height: 100%;width: 100% !important;\">\n"
				+ "                    <!--[if (!mso)&(!IE)]><!-->\n" + "                    <div\n"
				+ "                      style=\"box-sizing: border-box; height: 100%; padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\">\n"
				+ "                      <!--<![endif]-->\n" + "\n"
				+ "                      <table id=\"u_content_image_1\" style=\"font-family:'Raleway',sans-serif;\" role=\"presentation\"\n"
				+ "                        cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
				+ "                        <tbody>\n" + "                          <tr>\n"
				+ "                            <td class=\"v-container-padding-padding\"\n"
				+ "                              style=\"overflow-wrap:break-word;word-break:break-word;padding:50px 0px 0px 40px;font-family:'Raleway',sans-serif;\"\n"
				+ "                              align=\"left\">\n" + "\n"
				+ "                              <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n"
				+ "                                <tr>\n"
				+ "                                  <td style=\"padding-right: 0px;padding-left: 0px;\" align=\"left\">\n"
				+ "\n"
				+ "                                    <img align=\"left\" border=\"0\" src=\"\" alt=\"image\" title=\"image\"\n"
				+ "                                      style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: inline-block !important;border: none;height: auto;float: none;width: 17%;max-width: 95.2px;\"\n"
				+ "                                      width=\"95.2\" class=\"v-src-width v-src-max-width\" />\n"
				+ "\n" + "                                  </td>\n" + "                                </tr>\n"
				+ "                              </table>\n" + "\n" + "                            </td>\n"
				+ "                          </tr>\n" + "                        </tbody>\n"
				+ "                      </table>\n" + "\n"
				+ "                      <table id=\"u_content_heading_1\" style=\"font-family:'Raleway',sans-serif;\" role=\"presentation\"\n"
				+ "                        cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
				+ "                        <tbody>\n" + "                          <tr>\n"
				+ "                            <td class=\"v-container-padding-padding\"\n"
				+ "                              style=\"overflow-wrap:break-word;word-break:break-word;padding:15px 100px 10px 40px;font-family:'Raleway',sans-serif;\"\n"
				+ "                              align=\"left\">\n" + "\n"
				+ "                              <h1 class=\"v-font-size\"\n"
				+ "                                style=\"margin: 0px; line-height: 120%; text-align: center; word-wrap: break-word; font-size: 33px; font-weight: 700;\">\n"
				+ "                                <span style=\"line-height: 39.6px;\"></span>Recovery password</h1>\n"
				+ "\n" + "                            </td>\n" + "                          </tr>\n"
				+ "                        </tbody>\n" + "                      </table>\n" + "\n"
				+ "                      <table id=\"u_content_text_2\" style=\"font-family:'Raleway',sans-serif;\" role=\"presentation\"\n"
				+ "                        cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
				+ "                        <tbody>\n" + "                          <tr>\n"
				+ "                            <td class=\"v-container-padding-padding\"\n"
				+ "                              style=\"overflow-wrap:break-word;word-break:break-word;padding:10px 40px 0px;font-family:'Raleway',sans-serif;\"\n"
				+ "                              align=\"left\">\n" + "\n"
				+ "                              <div class=\"v-font-size\"\n"
				+ "                                style=\"font-size: 14px; line-height: 140%; text-align: left; word-wrap: break-word;\">\n"
				+ "                                <p style=\"line-height: 140%;\"><span></span><span style=\"line-height: 19.6px;\">Your recovery password code is: "
				+ code + ".<br /><br /></span>\n" + "                              </div>\n"
				+ "                            </td>\n" + "                          </tr>\n"
				+ "                        </tbody>\n" + "                      </table>\n" + "\n"
				+ "                      <!--[if (!mso)&(!IE)]><!-->\n" + "                    </div><!--<![endif]-->\n"
				+ "                  </div>\n" + "                </div>\n"
				+ "                <!--[if (mso)|(IE)]></td><![endif]-->\n"
				+ "                <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->\n"
				+ "              </div>\n" + "            </div>\n" + "          </div>\n" + "\n" + "\n" + "\n" + "\n"
				+ "\n"
				+ "          <div class=\"u-row-container\" style=\"padding: 0px;background-color: transparent\">\n"
				+ "            <div class=\"u-row\"\n"
				+ "              style=\"margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: transparent;\">\n"
				+ "              <div\n"
				+ "                style=\"border-collapse: collapse;display: table;width: 100%;height: 100%;background-color: transparent;\">\n"
				+ "                <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-color: transparent;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px;\"><tr style=\"background-color: transparent;\"><![endif]-->\n"
				+ "\n"
				+ "                <!--[if (mso)|(IE)]><td align=\"center\" width=\"600\" style=\"background-color: #ffffff;width: 600px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\" valign=\"top\"><![endif]-->\n"
				+ "                <div class=\"u-col u-col-100\"\n"
				+ "                  style=\"max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;\">\n"
				+ "                  <div\n"
				+ "                    style=\"background-color: #ffffff;height: 100%;width: 100% !important;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\">\n"
				+ "                    <!--[if (!mso)&(!IE)]><!-->\n" + "                    <div\n"
				+ "                      style=\"box-sizing: border-box; height: 100%; padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\">\n"
				+ "                      <!--<![endif]-->\n" + "\n"
				+ "                      <table id=\"u_content_social_1\" style=\"font-family:'Raleway',sans-serif;\" role=\"presentation\"\n"
				+ "                        cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
				+ "                        <tbody>\n" + "                          <tr>\n"
				+ "                            <td class=\"v-container-padding-padding\"\n"
				+ "                              style=\"overflow-wrap:break-word;word-break:break-word;padding:10px 10px 10px;font-family:'Raleway',sans-serif;\"\n"
				+ "                              align=\"left\">\n" + "\n"
				+ "                              <div align=\"center\">\n"
				+ "                                <div style=\"display: table; max-width:167px;\">\n"
				+ "                                  <!--[if (mso)|(IE)]><table width=\"167\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"border-collapse:collapse;\" align=\"center\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse; mso-table-lspace: 0pt;mso-table-rspace: 0pt; width:167px;\"><tr><![endif]-->\n"
				+ "\n" + "\n"
				+ "                                  <!--[if (mso)|(IE)]><td width=\"32\" style=\"width:32px; padding-right: 10px;\" valign=\"top\"><![endif]-->\n"
				+ "                                  <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"32\" height=\"32\"\n"
				+ "                                    style=\"width: 32px !important;height: 32px !important;display: inline-block;border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;margin-right: 10px\">\n"
				+ "                                    <tbody>\n"
				+ "                                      <tr style=\"vertical-align: top\">\n"
				+ "                                        <td align=\"left\" valign=\"middle\"\n"
				+ "                                          style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">\n"
				+ "                                          <a href=\"https://www.facebook.com\" title=\"Facebook\" target=\"_blank\">\n"
				+ "                                            <img src=\"images/image-3.png\" alt=\"Facebook\" title=\"Facebook\" width=\"32\"\n"
				+ "                                              style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: block !important;border: none;height: auto;float: none;max-width: 32px !important\">\n"
				+ "                                          </a>\n" + "                                        </td>\n"
				+ "                                      </tr>\n" + "                                    </tbody>\n"
				+ "                                  </table>\n"
				+ "                                  <!--[if (mso)|(IE)]></td><![endif]-->\n" + "\n"
				+ "                                  <!--[if (mso)|(IE)]><td width=\"32\" style=\"width:32px; padding-right: 10px;\" valign=\"top\"><![endif]-->\n"
				+ "                                  <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"32\" height=\"32\"\n"
				+ "                                    style=\"width: 32px !important;height: 32px !important;display: inline-block;border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;margin-right: 10px\">\n"
				+ "                                    <tbody>\n"
				+ "                                      <tr style=\"vertical-align: top\">\n"
				+ "                                        <td align=\"left\" valign=\"middle\"\n"
				+ "                                          style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">\n"
				+ "                                          <a href=\"https://twitter.com\" title=\"Twitter\" target=\"_blank\">\n"
				+ "                                            <img src=\"images/image-4.png\" alt=\"Twitter\" title=\"Twitter\" width=\"32\"\n"
				+ "                                              style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: block !important;border: none;height: auto;float: none;max-width: 32px !important\">\n"
				+ "                                          </a>\n" + "                                        </td>\n"
				+ "                                      </tr>\n" + "                                    </tbody>\n"
				+ "                                  </table>\n"
				+ "                                  <!--[if (mso)|(IE)]></td><![endif]-->\n" + "\n"
				+ "                                  <!--[if (mso)|(IE)]><td width=\"32\" style=\"width:32px; padding-right: 10px;\" valign=\"top\"><![endif]-->\n"
				+ "                                  <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"32\" height=\"32\"\n"
				+ "                                    style=\"width: 32px !important;height: 32px !important;display: inline-block;border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;margin-right: 10px\">\n"
				+ "                                    <tbody>\n"
				+ "                                      <tr style=\"vertical-align: top\">\n"
				+ "                                        <td align=\"left\" valign=\"middle\"\n"
				+ "                                          style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">\n"
				+ "                                          <a href=\"https://www.linkedin.com\" title=\"LinkedIn\"\n"
				+ "                                            target=\"_blank\">\n"
				+ "                                            <img src=\"images/image-5.png\" alt=\"LinkedIn\" title=\"LinkedIn\" width=\"32\"\n"
				+ "                                              style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: block !important;border: none;height: auto;float: none;max-width: 32px !important\">\n"
				+ "                                          </a>\n" + "                                        </td>\n"
				+ "                                      </tr>\n" + "                                    </tbody>\n"
				+ "                                  </table>\n"
				+ "                                  <!--[if (mso)|(IE)]></td><![endif]-->\n" + "\n"
				+ "                                  <!--[if (mso)|(IE)]><td width=\"32\" style=\"width:32px; padding-right: 0px;\" valign=\"top\"><![endif]-->\n"
				+ "                                  <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"32\" height=\"32\"\n"
				+ "                                    style=\"width: 32px !important;height: 32px !important;display: inline-block;border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;margin-right: 0px\">\n"
				+ "                                    <tbody>\n"
				+ "                                      <tr style=\"vertical-align: top\">\n"
				+ "                                        <td align=\"left\" valign=\"middle\"\n"
				+ "                                          style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">\n"
				+ "                                          <a href=\"https://www.instagram.com\" title=\"Instagram\"\n"
				+ "                                            target=\"_blank\">\n"
				+ "                                            <img src=\"images/image-7.png\" alt=\"Instagram\" title=\"Instagram\" width=\"32\"\n"
				+ "                                              style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: block !important;border: none;height: auto;float: none;max-width: 32px !important\">\n"
				+ "                                          </a>\n" + "                                        </td>\n"
				+ "                                      </tr>\n" + "                                    </tbody>\n"
				+ "                                  </table>\n"
				+ "                                  <!--[if (mso)|(IE)]></td><![endif]-->\n" + "\n" + "\n"
				+ "                                  <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->\n"
				+ "                                </div>\n" + "                              </div>\n" + "\n"
				+ "                            </td>\n" + "                          </tr>\n"
				+ "                        </tbody>\n" + "                      </table>\n" + "\n" + "\n" + "\n" + "\n"
				+ "                      <!--[if (!mso)&(!IE)]><!-->\n" + "                    </div><!--<![endif]-->\n"
				+ "                  </div>\n" + "                </div>\n"
				+ "                <!--[if (mso)|(IE)]></td><![endif]-->\n"
				+ "                <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->\n"
				+ "              </div>\n" + "            </div>\n" + "          </div>\n" + "\n" + "\n" + "\n"
				+ "          <!--[if (mso)|(IE)]></td></tr></table><![endif]-->\n" + "        </td>\n" + "      </tr>\n"
				+ "    </tbody>\n" + "  </table>\n" + "  <!--[if mso]></div><![endif]-->\n"
				+ "  <!--[if IE]></div><![endif]-->\n" + "</body>\n" + "";

		Thread thread = new Thread() {
			public void run() {
				sendMail("TEST@GMAIL.COM", email, "Khôi phục mật khẩu", content);
			}
		};

		thread.start();

		return "OK";
	}

	public String forgotPassword(ForgotPasswordRequest request) {
		Optional<User> user = repository.findByEmail(request.getEmail());
		if (user.isEmpty()) {
			throw new NotFoundException("Email không tồn tại!");
		}

		Optional<Confirmation> confirm = conRepo.findByEmail(request.getEmail());
		if (confirm.isEmpty()) {
			return "Yêu cầu khôi phục không tồn tại!";
		} else if (!confirm.get().getCode().equals(request.getCode())) {
			return "Mã xác thực không chính xác!";
		}

		user.get().setPassword(passwordEncoder.encode(request.getNewPassword()));

		repository.save(user.get());

		conRepo.delete(confirm.get());

		return "OK";
	}

	public HashMap<String, String> getInfo(String email) {
		HashMap<String, String> result = new HashMap<>();
		Optional<User> user = repository.findByEmail(email);
		if (user.isEmpty()) {
			throw new NotFoundException("Người dùng không tồn tại!");
		}
		if (user.get().getRefferal().equals("")) {
			result.put("isRefferal", "1");
		} else {
			result.put("isRefferal", "0");
		}
		result.put("refCode", user.get().getCode());
		result.put("firstName", user.get().getFirstname());
		result.put("lastName", user.get().getLastname());
		result.put("bio", user.get().getBio());
		return result;
	}

	private void revokeAllUserTokens(User user) {
		var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
		if (validUserTokens.isEmpty())
			return;
		validUserTokens.forEach(token -> {
			token.setExpired(true);
			token.setRevoked(true);
		});
		tokenRepository.saveAll(validUserTokens);
	}

	public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		final String refreshToken;
		final String userEmail;
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return;
		}
		refreshToken = authHeader.substring(7);
		userEmail = jwtService.extractUsername(refreshToken);
		if (userEmail != null) {
			var user = this.repository.findByEmail(userEmail).orElseThrow();
			if (jwtService.isTokenValid(refreshToken, user)) {
				var accessToken = jwtService.generateToken(user);
				revokeAllUserTokens(user);
				saveUserToken(user, accessToken);
				var authResponse = AuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken)
						.build();
				new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
			}
		}
	}

	public void sendMail(String from, String emailTo, String subject, String body) {
		EmailDto m = new EmailDto();
		m.setFrom(from);
		m.setSubject(subject);
		m.setTo(emailTo);
		m.setBody(body);
		try {
			mailService.send(m);
		} catch (Exception e) {
			System.out.println("Error : " + e.getMessage());
		}
	}

	public static String generateRandomNumberString(int length) {
		StringBuilder stringBuilder = new StringBuilder(length);
		Random random = new Random();

		for (int i = 0; i < length; i++) {
			int randomNumber = random.nextInt(10); // Tạo số ngẫu nhiên từ 0 đến 9
			stringBuilder.append(randomNumber);
		}

		return stringBuilder.toString();
	}

	public List<ExnessInfoDto> filterForSubBranch1(List<ExnessInfoDto> listExness, User rootUser) {
		List<ExnessInfoDto> exnessFiltered = new ArrayList<>();
		
		User userLevel1 = repository.findByRefferal(rootUser.getEmail()).get(0);
		findSubBranchExness(userLevel1, exnessFiltered);
		return exnessFiltered;
	}

	private void findSubBranchExness(User user, List<ExnessInfoDto> exnessFiltered) {
	    if (user != null) {
	        // Kiểm tra xem user có danh sách Exness không
	        List<Exness> userExnessList = user.getExnessList();
	        if (userExnessList != null) {
	            // Thêm Exness của user vào danh sách lọc
	            
	            for (Exness exness : userExnessList) {
	            	ExnessInfoDto item = new ExnessInfoDto();
	            	item.setExnessId(exness.getExness());
	            	item.setFullname(exness.getName());
	            	exnessFiltered.add(item);
	            }
	        }

	        // Tìm kiếm chiều sâu với tất cả người giới thiệu của user
	        List<User> refferalUsers = repository.findByRefferal(user.getEmail());
	        for (User userRefferal : refferalUsers) {
	            // Gọi đệ quy cho mỗi người giới thiệu
	            findSubBranchExness(userRefferal, exnessFiltered);
	        }
	    }
	}
}
