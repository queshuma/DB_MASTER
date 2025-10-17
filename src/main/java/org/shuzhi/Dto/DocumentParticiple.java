package org.shuzhi.Dto;

import lombok.Data;

import java.util.List;

@Data
public class DocumentParticiple {
    private String id;

    private List<Block> blocks;

    @Data
    public static class Block {
        private String id;

        private Integer sortNum;

        private String content;
    }
}
