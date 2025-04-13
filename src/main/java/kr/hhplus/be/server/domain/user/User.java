package kr.hhplus.be.server.domain.user;

import lombok.Getter;

@Getter
public class User {
    private Long id;

    public User(Long id){
        this.id = id;
    }

    public static User from(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다.");
        }
        return new User(id);
    }

    public Long getId() {
        return id;
    }
}
