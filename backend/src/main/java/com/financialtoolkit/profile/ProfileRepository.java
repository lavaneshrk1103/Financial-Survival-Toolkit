package com.financialtoolkit.profile;

import com.financialtoolkit.common.InMemoryRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileRepository extends InMemoryRepository<Profile, String> {
}
