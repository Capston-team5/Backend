package com.phishing.repository;

import com.phishing.domain.UrlAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlAnalysisRepository extends JpaRepository<UrlAnalysis, Long> {

    // 🌟 추가된 마법의 코드: isHelpful 값이 true(또는 false)인 데이터의 개수를 세어라!
    long countByIsHelpful(Boolean isHelpful);
}