package com.hanyang.dataportal.dataset.domain.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.hanyang.dataportal.core.exception.NullException;

import java.util.stream.Stream;

public enum License {
    저작자표시,저작자표시_비영리,저작자표시_변경금지,
    저작자표시_동일조건변경허락,
    저작자표시_비영리_동일조건변경허락,
    저작자표시_비영리_변경금지;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static License findByLicense(String License) {
        return Stream.of(com.hanyang.dataportal.dataset.domain.vo.License.values())
                .filter(o -> o.toString().equals(License))
                .findFirst()
                .orElseThrow(() -> new NullException("해당 라이센스는 존재하지 않습니다"));
    }
}
