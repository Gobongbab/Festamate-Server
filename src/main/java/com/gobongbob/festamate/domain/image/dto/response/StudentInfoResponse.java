package com.gobongbob.festamate.domain.image.dto.response;

public record StudentInfoResponse(
        String name,
        String studentDepartment,
        String studentId
) {

    public static StudentInfoResponse fromEntity(String name, String studentDepartment, String studentId) {
        return new StudentInfoResponse(
                name,
                studentDepartment,
                studentId
        );
    }
}
