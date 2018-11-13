package life.genny.qwandautils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;

/**
 * ScoringUtils provides scoring functions. BaseEntitys can be scored against
 * each other. Current scoring is performed using a standard Matrix Decision
 * Table mechanism.
 * 
 * 
 * @author Adam Crow
 * @author Byron Aguirre
 * @version %I%, %G%
 * @since 1.0
 */
public class ScoringUtils {
  
    private ScoringUtils() {}

	/**
	 * calculateScore This function compares each baseEntity attribute and if they
	 * match will then check if their values match or are within range. If there is
	 * a match then the datatypes determine what attribute score to perform. The
	 * weighting of each attribute contributes to the score.
	 * 
	 * @param sourceBaseEntity
	 *            (usually a person, truck etc)
	 * @param targetBaseEntity
	 *            (usually the bseEntity with fewer attributes).
	 * @return Double rawScore
	 */
	public static Double calculateScore(BaseEntity sourceBaseEntity, BaseEntity targetBaseEntity) {
		Double rawScore = 0.0;
		// Ignore correlated attributes for now

		// go through each attribute and calculate score
		for (EntityAttribute targetEA : targetBaseEntity.getBaseEntityAttributes()) {
			if (targetEA.getWeight() != 0) {
				for (EntityAttribute sourceEA : sourceBaseEntity.getBaseEntityAttributes()) {
					if (sourceEA.getWeight() != 0) {
						// for debugging and clarity, expand out code
						if (targetEA.getAttributeCode().equalsIgnoreCase(sourceEA.getAttributeCode())) { // attributes
							Double attributeScore = 0.0;
							switch (targetEA.getAttribute().getDataType().getClassName()) {
							case "java.lang.Boolean":
								attributeScore = (targetEA.getValueBoolean() ? targetEA.getWeight() : 0.0)
										* (sourceEA.getValueBoolean() ? sourceEA.getWeight() : 0.0);
								break;
							case "java.lang.Double":
								if (targetEA.getValueDouble() == sourceEA.getValueDouble()) {
									attributeScore = targetEA.getValueDouble() * targetEA.getWeight()
											* (sourceEA.getValueDouble() * sourceEA.getWeight());
								}
								break;
							case "java.lang.Long":
								if (targetEA.getValueLong() == sourceEA.getValueLong()) {
									attributeScore = targetEA.getValueLong().doubleValue() * targetEA.getWeight()
											* (sourceEA.getValueLong().doubleValue() * sourceEA.getWeight());
								}
								break;
							case "java.lang.String":
								if (targetEA.getValueString().equalsIgnoreCase(sourceEA.getValueString())) {
									attributeScore = targetEA.getWeight() * sourceEA.getWeight();
								}
								break;
							case "java.lang.Integer":
								if (targetEA.getValueInteger() == sourceEA.getValueInteger()) {
									attributeScore = targetEA.getValueInteger().doubleValue() * targetEA.getWeight()
											* (sourceEA.getValueInteger().doubleValue() * sourceEA.getWeight());
								}
								break;
							case "java.time.LocalDateTime":
								if (targetEA.getValueDateTime().isEqual(sourceEA.getValueDateTime())) {
									attributeScore = targetEA.getWeight() * sourceEA.getWeight();
								}
								break;
							case "java.time.LocalDate":
								if (targetEA.getValueDate().isEqual(sourceEA.getValueDate())) {
									attributeScore = targetEA.getWeight() * sourceEA.getWeight();
								}
								break;
							}

							rawScore += attributeScore;
						}
					}
				}
			}
		}

		// for each attribute apply weightings using standard decision matrix score
		BigDecimal bd = BigDecimal.valueOf(rawScore).setScale(2, RoundingMode.HALF_EVEN);
		rawScore = bd.doubleValue();
		return rawScore;
	}
}
