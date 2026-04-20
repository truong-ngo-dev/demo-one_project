package vn.truongngo.apartcom.one.service.admin.domain.party;

import java.util.List;

public interface PartyClient {

    List<String> getMembers(String partyId);
}
