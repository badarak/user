package com.badarak.domain.port.in;

import com.badarak.domain.model.UserPage;
import com.badarak.domain.model.UserQuery;

public interface ListUsersUseCase {
    UserPage execute(UserQuery query);
}
