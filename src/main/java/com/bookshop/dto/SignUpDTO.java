package com.bookshop.dto;

import com.bookshop.constants.Common;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SignUpDTO {
    @NotBlank(message = " không được để trống")
    @Length(max = Common.STRING_LENGTH_LIMIT)
    private String firstName;

    @NotBlank(message = " không được để trống")
    @Length(max = Common.STRING_LENGTH_LIMIT)
    private String lastName;

    @NotBlank(message = " không được để trống")
    @Length(max = Common.STRING_LENGTH_LIMIT)
    private String username;

    @NotBlank(message = " không được để trống")
    @Length(max = Common.STRING_LENGTH_LIMIT)
    private String address;

    @NotBlank(message = " không được để trống")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&_]{8,}$", message = ": Mật khẩu không hợp lệ")
    @Length(max = Common.STRING_LENGTH_LIMIT)
    private String password;

    @NotBlank(message = " không được để trống")
    @Email(message = ": Không đúng định dạng")
    @Length(max = Common.STRING_LENGTH_LIMIT)
    private String email;

    @NotBlank(message = " không được để trống")
    @Pattern(regexp = "(^$|[0-9]{10})", message = ": Số điện thoại không hợp lệ")
    @Length(max = Common.STRING_LENGTH_LIMIT)
    private String phone;
}
