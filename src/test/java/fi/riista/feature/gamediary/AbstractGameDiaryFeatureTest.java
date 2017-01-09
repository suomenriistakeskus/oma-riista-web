package fi.riista.feature.gamediary;

import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.util.F;
import org.junit.Test;

import java.util.List;

public class AbstractGameDiaryFeatureTest {

    @Test(expected = HarvestPermitSpecimensIncompleteException.class)
    public void testWeightRequiredYes() {
        HarvestReportFields fields = fieldsWithWeightRequired(Required.YES);
        HarvestDTO dto = dtoWithWeight(1.2, null, 10.2);
        AbstractGameDiaryFeature.assertSpecimensWithFields(dto.getSpecimens(), fields);
    }

    @Test
    public void testWeightRequiredVoluntary() {
        HarvestReportFields fields = fieldsWithWeightRequired(Required.VOLUNTARY);
        HarvestDTO dto = dtoWithWeight(1.2, null, 10.2);
        AbstractGameDiaryFeature.assertSpecimensWithFields(dto.getSpecimens(), fields);
    }

    private static HarvestReportFields fieldsWithWeightRequired(Required required) {
        HarvestReportFields fields = fields();
        fields.setWeight(required);
        return fields;
    }

    private static List<HarvestSpecimenDTO> specimens(Double[] weights) {
        return F.mapNonNullsToList(weights, weight -> new HarvestSpecimenDTO(null, null, weight));
    }

    @Test(expected = HarvestPermitSpecimensIncompleteException.class)
    public void testAgeRequiredYes() {
        HarvestReportFields fields = fieldsWithAgeRequired(Required.YES);
        HarvestDTO dto = dtoWithAges(GameAge.ADULT, null, GameAge.UNKNOWN);
        AbstractGameDiaryFeature.assertSpecimensWithFields(dto.getSpecimens(), fields);
    }

    @Test
    public void testAgeRequiredVoluntary() {
        HarvestReportFields fields = fieldsWithAgeRequired(Required.VOLUNTARY);
        HarvestDTO dto = dtoWithAges(GameAge.ADULT, null, GameAge.UNKNOWN);
        AbstractGameDiaryFeature.assertSpecimensWithFields(dto.getSpecimens(), fields);
    }

    private static HarvestReportFields fieldsWithAgeRequired(Required required) {
        HarvestReportFields fields = fields();
        fields.setAge(required);
        return fields;
    }

    private static List<HarvestSpecimenDTO> specimens(GameAge[] ages) {
        return F.mapNonNullsToList(ages, age -> new HarvestSpecimenDTO(null, age, null));
    }

    @Test(expected = HarvestPermitSpecimensIncompleteException.class)
    public void testRequiredRequiredYes() {
        HarvestReportFields fields = fieldsWithGenderRequired(Required.YES);
        HarvestDTO dto = dtoWithGenders(GameGender.FEMALE, null, GameGender.MALE);
        AbstractGameDiaryFeature.assertSpecimensWithFields(dto.getSpecimens(), fields);
    }

    @Test
    public void testGenderRequiredVoluntary() {
        HarvestReportFields fields = fieldsWithGenderRequired(Required.VOLUNTARY);
        HarvestDTO dto = dtoWithGenders(GameGender.FEMALE, null, GameGender.MALE);
        AbstractGameDiaryFeature.assertSpecimensWithFields(dto.getSpecimens(), fields);
    }

    private static HarvestDTO dtoWithWeight(Double... weights) {
        return HarvestDTO.builder().withSpecimens(specimens(weights)).build();
    }

    private static HarvestDTO dtoWithAges(GameAge... weights) {
        return HarvestDTO.builder().withSpecimens(specimens(weights)).build();
    }

    private static HarvestDTO dtoWithGenders(GameGender... genders) {
        return HarvestDTO.builder().withSpecimens(specimens(genders)).build();
    }

    private static HarvestReportFields fieldsWithGenderRequired(Required required) {
        HarvestReportFields fields = fields();
        fields.setGender(required);
        return fields;
    }

    private static List<HarvestSpecimenDTO> specimens(GameGender[] genders) {
        return F.mapNonNullsToList(genders, gender -> new HarvestSpecimenDTO(gender, null, null));
    }

    private static HarvestReportFields fields() {
        HarvestReportFields fields = new HarvestReportFields("", null, true);
        fields.setWeight(Required.NO);
        fields.setAge(Required.NO);
        fields.setGender(Required.NO);
        return fields;
    }
}
