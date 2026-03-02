package com.badarak.domain.port.in;

import com.badarak.domain.model.UserId;

public interface DeleteUserUseCase {

    void execute(UserId id);
}
