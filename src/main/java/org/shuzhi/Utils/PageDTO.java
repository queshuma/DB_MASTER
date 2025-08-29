package org.shuzhi.Utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Getter;
import lombok.Setter;

/**
 * Description:
 * Author: chentao
 * Date: 11 2月 2025
 *
 * @version 1.0
 */
@Setter
@Getter
public class PageDTO {

    private int page;

    private int size;

    public <T> IPage<T> getPage() {
        this.page = this.page <= 0 ? 1 : this.page;
        this.size = this.size <= 0 ? 10 : this.size;
        return new Page<>(this.page, this.size);
    }
}
