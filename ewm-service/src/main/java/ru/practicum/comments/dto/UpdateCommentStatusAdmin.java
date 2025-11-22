package ru.practicum.comments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.requests.dto.RequestStatus;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentStatusAdmin {
    private List<Long> commentsIds;
    private CommentStatus status;
}
