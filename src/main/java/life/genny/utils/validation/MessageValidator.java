package life.genny.utils.validation;

import life.genny.qwanda.entity.BaseEntity;

public class MessageValidator {

    public static Boolean isEmailBlank(BaseEntity baseEntity) {
        Boolean isBlank = isBlank(baseEntity, "PRI_EMAIL");
        if (isBlank) {
            System.out.println("Email not found!!");
        }
        return isBlank;
    }

    public static Boolean isMobileBlank(BaseEntity baseEntity) {
        Boolean isBlank = isBlank(baseEntity, "PRI_MOBILE");
        if (isBlank) {
            System.out.println("Mobile no not found!!");
        }
        return isBlank;
    }

    public static Boolean isBlank(BaseEntity baseEntity, String attribute) {
        if(baseEntity == null) {
            System.out.println("BaseEntity is null!!");
            return false;
        }
        String element = baseEntity.getValueAsString(attribute);
        if (element != null && !element.isEmpty()) {
            return false;
        }
        return true;
    }
}
