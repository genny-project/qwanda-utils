package life.genny.qwandautils;

import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.exception.BadDataException;

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
			if ((targetEA.getWeight() != 0)) {
				for (EntityAttribute sourceEA : sourceBaseEntity.getBaseEntityAttributes()) {
					if (sourceEA.getWeight() != 0) {
						// for debugging and clarity, expand out code
						if (targetEA.getAttributeCode().equalsIgnoreCase(sourceEA.getAttributeCode())) { // attributes
																											// must
																											// match
							// TODO: enums faster...
							Double attributeScore = 0.0;
							switch (targetEA.getAttribute().getDataType().getClassName()) {
							case "java.lang.Boolean" : 
							}
						}
					}
				}
			}
		}

		// for each attribute apply weightings using standard decision matrix score

		return rawScore;
	}
}
