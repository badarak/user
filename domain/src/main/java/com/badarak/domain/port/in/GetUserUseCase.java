package com.badarak.domain.port.in;

import com.badarak.domain.model.User;
import com.badarak.domain.model.UserId;

public interface GetUserUseCase {
    User execute(UserId userId);
}
