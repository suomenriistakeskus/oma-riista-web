package fi.riista.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

import com.querydsl.sql.spatial.RelationalPathSpatial;

import com.querydsl.spatial.*;



/**
 * SQSystemUser is a Querydsl query type for SQSystemUser
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQSystemUser extends RelationalPathSpatial<SQSystemUser> {

    private static final long serialVersionUID = 849673645;

    public static final SQSystemUser systemUser = new SQSystemUser("system_user");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath email = createString("email");

    public final StringPath firstName = createString("firstName");

    public final StringPath ipWhiteList = createString("ipWhiteList");

    public final BooleanPath isActive = createBoolean("isActive");

    public final StringPath lastName = createString("lastName");

    public final StringPath localeId = createString("localeId");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath password = createString("password");

    public final NumberPath<Long> personId = createNumber("personId", Long.class);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath role = createString("role");

    public final StringPath timezoneId = createString("timezoneId");

    public final StringPath twoFactorAuthentication = createString("twoFactorAuthentication");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final StringPath username = createString("username");

    public final com.querydsl.sql.PrimaryKey<SQSystemUser> systemUserPkey = createPrimaryKey(userId);

    public final com.querydsl.sql.ForeignKey<SQPerson> systemUserPersonFk = createForeignKey(personId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQTwoFactorAuthenticationMode> systemUserTwoFactorFk = createForeignKey(twoFactorAuthentication, "name");

    public final com.querydsl.sql.ForeignKey<SQSystemUserRole> systemUserRoleFk = createForeignKey(role, "name");

    public final com.querydsl.sql.ForeignKey<SQPersistentRememberMeToken> _persistentTokenUserFk = createInvForeignKey(userId, "user_id");

    public final com.querydsl.sql.ForeignKey<SQSystemUserPrivilege> _systemUserPrivilegeSystemUserFk = createInvForeignKey(userId, "user_id");

    public final com.querydsl.sql.ForeignKey<SQVetumaTransaction> _vetumaTransactionSystemUserFk = createInvForeignKey(userId, "user_id");

    public final com.querydsl.sql.ForeignKey<SQOccupationNomination> _occupationNominationUserIdFk = createInvForeignKey(userId, "moderator_user_id");

    public final com.querydsl.sql.ForeignKey<SQEmailToken> _emailTokenSystemUserFk = createInvForeignKey(userId, "user_id");

    public final com.querydsl.sql.ForeignKey<SQSmsMessage> _smsMessageSystemUserFk = createInvForeignKey(userId, "user_id");

    public final com.querydsl.sql.ForeignKey<SQAnnouncement> _announcementUserIdFk = createInvForeignKey(userId, "from_user_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> _permitDecisionHandlerIdFk = createInvForeignKey(userId, "handler_id");

    public final com.querydsl.sql.ForeignKey<SQSrvaEvent> _srvaEventApproverAsUserFk = createInvForeignKey(userId, "approver_as_user_id");

    public SQSystemUser(String variable) {
        super(SQSystemUser.class, forVariable(variable), "public", "system_user");
        addMetadata();
    }

    public SQSystemUser(String variable, String schema, String table) {
        super(SQSystemUser.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQSystemUser(String variable, String schema) {
        super(SQSystemUser.class, forVariable(variable), schema, "system_user");
        addMetadata();
    }

    public SQSystemUser(Path<? extends SQSystemUser> path) {
        super(path.getType(), path.getMetadata(), "public", "system_user");
        addMetadata();
    }

    public SQSystemUser(PathMetadata metadata) {
        super(SQSystemUser.class, metadata, "public", "system_user");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(email, ColumnMetadata.named("email").withIndex(15).ofType(Types.VARCHAR).withSize(255));
        addMetadata(firstName, ColumnMetadata.named("first_name").withIndex(13).ofType(Types.VARCHAR).withSize(255));
        addMetadata(ipWhiteList, ColumnMetadata.named("ip_white_list").withIndex(20).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(isActive, ColumnMetadata.named("is_active").withIndex(9).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(lastName, ColumnMetadata.named("last_name").withIndex(14).ofType(Types.VARCHAR).withSize(255));
        addMetadata(localeId, ColumnMetadata.named("locale_id").withIndex(17).ofType(Types.VARCHAR).withSize(255));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(password, ColumnMetadata.named("password").withIndex(11).ofType(Types.VARCHAR).withSize(255));
        addMetadata(personId, ColumnMetadata.named("person_id").withIndex(19).ofType(Types.BIGINT).withSize(19));
        addMetadata(phoneNumber, ColumnMetadata.named("phone_number").withIndex(16).ofType(Types.VARCHAR).withSize(255));
        addMetadata(role, ColumnMetadata.named("role").withIndex(12).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(timezoneId, ColumnMetadata.named("timezone_id").withIndex(18).ofType(Types.VARCHAR).withSize(255));
        addMetadata(twoFactorAuthentication, ColumnMetadata.named("two_factor_authentication").withIndex(21).ofType(Types.VARCHAR).withSize(255));
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(username, ColumnMetadata.named("username").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

