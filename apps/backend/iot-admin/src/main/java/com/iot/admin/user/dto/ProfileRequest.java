package com.iot.admin.user.dto;

import jakarta.validation.constraints.Size;

public record ProfileRequest(@Size(max = 100) String displayName) {
}
