package org.example.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Call {
    private String id;
    private String caller;
    private String receiver;
    private Integer duration;
    private Long sendTime;
}
