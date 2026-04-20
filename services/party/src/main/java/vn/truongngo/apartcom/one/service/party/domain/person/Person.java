package vn.truongngo.apartcom.one.service.party.domain.person;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractAggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.AggregateRoot;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;

import java.time.LocalDate;

@Getter
public class Person extends AbstractAggregateRoot<PartyId> implements AggregateRoot<PartyId> {

    private String firstName;
    private String lastName;
    private LocalDate dob;
    private Gender gender;

    private Person(PartyId id, String firstName, String lastName, LocalDate dob, Gender gender) {
        super(id);
        this.firstName = firstName;
        this.lastName  = lastName;
        this.dob       = dob;
        this.gender    = gender;
    }

    public static Person create(PartyId id, String firstName, String lastName,
                                LocalDate dob, Gender gender) {
        Assert.hasText(firstName, "firstName is required");
        Assert.hasText(lastName, "lastName is required");
        return new Person(id, firstName, lastName, dob, gender);
    }

    public static Person reconstitute(PartyId id, String firstName, String lastName,
                                      LocalDate dob, Gender gender) {
        return new Person(id, firstName, lastName, dob, gender);
    }

    public void updateProfile(String firstName, String lastName, LocalDate dob, Gender gender) {
        Assert.hasText(firstName, "firstName is required");
        Assert.hasText(lastName, "lastName is required");
        this.firstName = firstName;
        this.lastName  = lastName;
        this.dob       = dob;
        this.gender    = gender;
    }
}
