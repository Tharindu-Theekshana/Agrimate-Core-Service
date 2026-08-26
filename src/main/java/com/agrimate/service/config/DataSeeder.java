package com.agrimate.service.config;

import com.agrimate.service.model.appVersion.AppVersion;
import com.agrimate.service.model.disease.Disease;
import com.agrimate.service.model.role.Role;
import com.agrimate.service.model.appVersion.Platform;
import com.agrimate.service.model.role.RoleName;
import com.agrimate.service.model.disease.Severity;
import com.agrimate.service.repository.AppVersionRepository;
import com.agrimate.service.repository.DiseaseRepository;
import com.agrimate.service.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final DiseaseRepository diseaseRepository;
    private final RoleRepository roleRepository;
    private final AppVersionRepository appVersionRepository;

    public DataSeeder(DiseaseRepository diseaseRepository, RoleRepository roleRepository,
                      AppVersionRepository appVersionRepository) {
        this.diseaseRepository = diseaseRepository;
        this.roleRepository = roleRepository;
        this.appVersionRepository = appVersionRepository;
    }

    @Override
    public void run(String... args) {
        seedRoles();
        seedDiseases();
        seedAppVersions();
    }

    private void seedAppVersions() {
        if (appVersionRepository.count() > 0) return;
        for (Platform platform : Platform.values()) {
            AppVersion v = new AppVersion();
            v.setPlatform(platform);
            v.setVersion("1.0.0");
            v.setLatest(true);
            v.setForceUpdate(false);
            appVersionRepository.save(v);
        }
        log.info("Seeded baseline app versions (1.0.0) for ANDROID/IOS.");
    }

    private void seedRoles() {
        for (RoleName name : RoleName.values()) {
            if (!roleRepository.existsByName(name)) {
                roleRepository.save(new Role(name, name.name().toLowerCase() + " role"));
            }
        }
    }

    private void seedDiseases() {
        if (diseaseRepository.count() > 0) return;

        diseaseRepository.save(disease("rice_blast",
                "Rice Blast", "වී පුස්ස රෝගය", "நெல் குலை நோய்",
                "Magnaporthe oryzae", Severity.HIGH,
                new Text(
                        "Caused by the fungus Magnaporthe oryzae. Favoured by high humidity, leaf wetness, "
                                + "cool night temperatures and excessive nitrogen fertiliser.",
                        "Magnaporthe oryzae දිලීරයෙන් ඇතිවේ. අධික ආර්ද්‍රතාවය, පත්‍ර තෙතමනය, සිසිල් රාත්‍රී "
                                + "උෂ්ණත්වය සහ අධික නයිට්‍රජන් පොහොර භාවිතය මගින් රෝගය වර්ධනය වේ.",
                        "Magnaporthe oryzae எனும் பூஞ்சையால் ஏற்படுகிறது. அதிக ஈரப்பதம், இலை ஈரம், குளிர்ந்த "
                                + "இரவு வெப்பநிலை மற்றும் அதிகப்படியான நைட்ரஜன் உரப் பயன்பாடு ஆகியவை இந்த நோயை அதிகரிக்கச் செய்கின்றன."),
                new Text(
                        "Diamond / spindle-shaped lesions with grey centres and brown margins on leaves. "
                                + "Can also infect the neck of the panicle (neck blast), causing whiteheads and yield loss.",
                        "පත්‍රවල අළු පැහැති මධ්‍යයක් සහ දුඹුරු කෙළවරක් සහිත වයඹ හැඩැති හෝ දියමන්ති හැඩැති තුවාල "
                                + "ඇතිවේ. කරල් හණුවේද (neck blast) ආසාදනය විය හැකි අතර, එමගින් සුදු කරල් සහ අස්වැන්න පිරිහීම සිදුවේ.",
                        "இலைகளில் சாம்பல் நிற மையமும் பழுப்பு நிற ஓரங்களும் கொண்ட வைர வடிவ அல்லது தண்டு வடிவ "
                                + "புண்கள் தோன்றும். கதிர் கழுத்தையும் (neck blast) பாதிக்கக்கூடும், இது வெண் கதிர்களுக்கும் "
                                + "விளைச்சல் இழப்புக்கும் காரணமாகும்."),
                new Text(
                        "Remove and destroy infected debris. Apply a recommended fungicide at early lesion "
                                + "stage following local Department of Agriculture guidance. Avoid over-application of nitrogen.",
                        "ආසාදිත අවශේෂ ඉවත් කර විනාශ කරන්න. තුවාල මුල් අවධියේදී ප්‍රාදේශීය කෘෂිකර්ම දෙපාර්තමේන්තුවේ "
                                + "උපදෙස් අනුව නිර්දේශිත දිලීර නාශකයක් යොදන්න. නයිට්‍රජන් අධික ලෙස භාවිතා කිරීමෙන් වළකින්න.",
                        "பாதிக்கப்பட்ட எச்சங்களை அகற்றி அழிக்கவும். புண் ஏற்படும் ஆரம்ப கட்டத்தில் உள்ளூர் வேளாண் "
                                + "திணைக்களத்தின் வழிகாட்டுதலின்படி பரிந்துரைக்கப்பட்ட பூஞ்சைக் கொல்லியைப் பயன்படுத்தவும். "
                                + "அதிகப்படியான நைட்ரஜன் பயன்பாட்டைத் தவிர்க்கவும்."),
                new Text(
                        "Use resistant varieties, balanced fertiliser, good field drainage and certified seed. "
                                + "Avoid dense planting to improve air circulation.",
                        "ප්‍රතිරෝධී ප්‍රභේද, සමතුලිත පොහොර, හොඳ කුඹුරු ජලාපවහනය සහ සහතික කළ බීජ භාවිතා කරන්න. "
                                + "වාතාශ්‍රය වැඩි කිරීමට තදබදව සිටුවීමෙන් වළකින්න.",
                        "எதிர்ப்புத் திறன் கொண்ட ரகங்கள், சமச்சீர் உரம், நல்ல வயல் வடிகால் மற்றும் சான்றளிக்கப்பட்ட "
                                + "விதைகளைப் பயன்படுத்தவும். காற்றோட்டத்தை மேம்படுத்த அடர்த்தியான நடவை தவிர்க்கவும்.")));

        diseaseRepository.save(disease("bacterial_leaf_blight",
                "Bacterial Leaf Blight", "බැක්ටීරියා පත්‍ර අංගමාරය", "பாக்டீரியா இலை கருகல்",
                "Xanthomonas oryzae pv. oryzae", Severity.HIGH,
                new Text(
                        "Caused by the bacterium Xanthomonas oryzae. Spreads through wind, rain splash, "
                                + "irrigation water and wounds; severe in flooded fields after storms.",
                        "Xanthomonas oryzae බැක්ටීරියාවෙන් ඇතිවේ. සුළඟ, වර්ෂා ජල ඉසිලීම, වාරි ජලය සහ තුවාල "
                                + "හරහා පැතිරේ; කුණාටු වලින් පසු ජලයෙන් යටවූ කුඹුරුවල දරුණු වේ.",
                        "Xanthomonas oryzae எனும் பாக்டீரியாவால் ஏற்படுகிறது. காற்று, மழைத் தெறிப்பு, பாசன "
                                + "நீர் மற்றும் காயங்கள் மூலம் பரவுகிறது; புயல்களுக்குப் பின் வெள்ளத்தில் மூழ்கிய "
                                + "வயல்களில் கடுமையாக இருக்கும்."),
                new Text(
                        "Water-soaked yellow lesions starting at leaf tips and margins that turn straw-coloured "
                                + "and spread along the veins. Wilting of seedlings (kresek) in severe cases.",
                        "පත්‍ර අග්‍ර සහ දාරවලින් ආරම්භ වන ජලයෙන් තෙත් වූ කහ පැහැති තුවාල පසුව පිදුරු පැහැයට "
                                + "හැරී නහරවල් දිගේ පැතිරේ. දරුණු අවස්ථාවලදී පැළ (kresek) මැලවීම සිදුවේ.",
                        "இலை நுனிகள் மற்றும் ஓரங்களில் தொடங்கும் நீரூறல் மஞ்சள் புண்கள் பின்னர் வைக்கோல் "
                                + "நிறமாக மாறி நரம்புகளின் வழியே பரவும். கடுமையான நிலைகளில் நாற்றுகள் வாடும் (kresek) "
                                + "நிலை ஏற்படும்."),
                new Text(
                        "There is no effective chemical cure once established — focus on field sanitation. "
                                + "Drain fields, remove infected plants, and use balanced fertiliser. Follow local guidance.",
                        "රෝගය ස්ථාපිත වූ පසු ඵලදායී රසායනික ප්‍රතිකාරයක් නොමැත — කුඹුර පිරිසිදුව තබා ගැනීම "
                                + "කෙරෙහි අවධානය යොමු කරන්න. කුඹුරු ජලය බැස ගන්වන්න, ආසාදිත ශාක ඉවත් කරන්න, සමතුලිත "
                                + "පොහොර භාවිතා කරන්න. ප්‍රාදේශීය මාර්ගෝපදේශ අනුගමනය කරන්න.",
                        "நோய் ஏற்பட்ட பின் பயனுள்ள இரசாயன மருந்து இல்லை — வயல் சுத்தத்தில் கவனம் செலுத்தவும். "
                                + "வயல்களை வடிகட்டவும், பாதிக்கப்பட்ட செடிகளை அகற்றவும், சமச்சீர் உரத்தைப் பயன்படுத்தவும். "
                                + "உள்ளூர் வழிகாட்டுதலைப் பின்பற்றவும்."),
                new Text(
                        "Plant resistant varieties, use disease-free seed, avoid clipping seedling tips, "
                                + "and manage water and nitrogen carefully.",
                        "ප්‍රතිරෝධී ප්‍රභේද සිටුවන්න, රෝගවලින් තොර බීජ භාවිතා කරන්න, පැළ අග්‍ර කැපීමෙන් වළකින්න, "
                                + "ජලය සහ නයිට්‍රජන් හොඳින් කළමනාකරණය කරන්න.",
                        "எதிர்ப்புத் திறன் கொண்ட ரகங்களை நடவும், நோயற்ற விதைகளைப் பயன்படுத்தவும், நாற்று "
                                + "நுனிகளை வெட்டுவதைத் தவிர்க்கவும், நீர் மற்றும் நைட்ரஜனை கவனமாக நிர்வகிக்கவும்.")));

        diseaseRepository.save(disease("brown_spot",
                "Brown Spot", "දුඹුරු තිත් රෝගය", "பழுப்பு புள்ளி நோய்",
                "Bipolaris oryzae (Cochliobolus miyabeanus)", Severity.MEDIUM,
                new Text(
                        "Fungal disease caused by Bipolaris oryzae. Strongly linked to nutrient-poor soils "
                                + "(especially potassium deficiency) and water stress.",
                        "Bipolaris oryzae දිලීරයෙන් ඇතිවන රෝගයකි. පෝෂක ඌනතාවයක් ඇති පස් (විශේෂයෙන් පොටෑසියම් "
                                + "ඌනතාවය) සහ ජල ආතතිය සමඟ තදින් සම්බන්ධ වේ.",
                        "Bipolaris oryzae எனும் பூஞ்சையால் ஏற்படும் நோய். ஊட்டச்சத்து குறைந்த மண் (குறிப்பாக "
                                + "பொட்டாசியம் குறைபாடு) மற்றும் நீர் அழுத்தத்துடன் நெருங்கிய தொடர்புடையது."),
                new Text(
                        "Small, circular to oval brown spots with grey/tan centres on leaves and grains. "
                                + "Heavy infection gives leaves a 'burnt' appearance and produces discoloured grain.",
                        "පත්‍ර සහ ධාන්‍යවල අළු/දුඹුරු පැහැති මධ්‍යයක් සහිත කුඩා, වෘත්තාකාර හෝ ඕවලාකාර දුඹුරු "
                                + "තිත් ඇතිවේ. දරුණු ආසාදනයකදී පත්‍ර 'දැවුණු' පෙනුමක් ගනී සහ වර්ණය වෙනස් වූ ධාන්‍ය නිපදවයි.",
                        "இலைகள் மற்றும் தானியங்களில் சாம்பல்/பழுப்பு நிற மையம் கொண்ட சிறிய, வட்ட அல்லது "
                                + "நீள்வட்ட பழுப்பு புள்ளிகள் தோன்றும். கடுமையான தொற்று இலைகளுக்கு 'எரிந்த' தோற்றத்தை "
                                + "அளித்து நிறம் மாறிய தானியத்தை உருவாக்கும்."),
                new Text(
                        "Correct soil nutrient deficiencies (notably potassium). Apply a recommended fungicide "
                                + "where necessary per local guidance, and treat seed before planting.",
                        "පස පෝෂක ඌනතා (විශේෂයෙන් පොටෑසියම්) නිවැරදි කරන්න. අවශ්‍ය නම් ප්‍රාදේශීය මාර්ගෝපදේශ "
                                + "අනුව නිර්දේශිත දිලීර නාශකයක් යොදන්න, සහ සිටුවීමට පෙර බීජ ප්‍රතිකාර කරන්න.",
                        "மண் ஊட்டச்சத்து குறைபாடுகளை (குறிப்பாக பொட்டாசியம்) சரிசெய்யவும். தேவைப்படின் "
                                + "உள்ளூர் வழிகாட்டுதலின்படி பரிந்துரைக்கப்பட்ட பூஞ்சைக் கொல்லியைப் பயன்படுத்தவும், "
                                + "நடவுக்கு முன் விதையை சிகிச்சை செய்யவும்."),
                new Text(
                        "Use balanced fertilisation, good soil management, certified seed, and proper water "
                                + "management to reduce plant stress.",
                        "ශාක ආතතිය අවම කිරීමට සමතුලිත පොහොර යෙදීම, හොඳ පස් කළමනාකරණය, සහතික කළ බීජ සහ නිසි "
                                + "ජල කළමනාකරණය භාවිතා කරන්න.",
                        "செடியின் அழுத்தத்தைக் குறைக்க சமச்சீர் உரமிடுதல், நல்ல மண் மேலாண்மை, சான்றளிக்கப்பட்ட "
                                + "விதைகள் மற்றும் சரியான நீர் மேலாண்மையைப் பயன்படுத்தவும்.")));

        diseaseRepository.save(disease("tungro",
                "Tungro", "ටුන්ග්‍රෝ රෝගය", "துங்ரோ நோய்",
                "Rice tungro virus (RTBV + RTSV)", Severity.HIGH,
                new Text(
                        "Viral disease transmitted by green leafhoppers (Nephotettix spp.). The insect vector "
                                + "spreads the virus rapidly between plants.",
                        "කොළ පැහැති පත්‍ර පනුවන් (Nephotettix spp.) මගින් සම්ප්‍රේෂණය වන වෛරස් රෝගයකි. මෙම "
                                + "කෘමි වාහකයා ශාක අතර වේගයෙන් වෛරසය පතුරුවයි.",
                        "பச்சை இலைத்தத்துப்பூச்சிகளால் (Nephotettix spp.) பரவும் வைரஸ் நோய். இந்த பூச்சி "
                                + "வாகனம் தாவரங்களுக்கிடையே வைரஸை வேகமாக பரப்புகிறது."),
                new Text(
                        "Yellow to orange discolouration of leaves starting from the tip, stunted growth, "
                                + "reduced tillering and delayed flowering.",
                        "අග්‍රයේ සිට ආරම්භ වන පත්‍රවල කහ සිට තැඹිලි පැහැයට වර්ණ වෙනස්වීම, වර්ධනය මන්දගාමී "
                                + "වීම, කරල් හැදීම අඩුවීම සහ මල් පිපීම ප්‍රමාද වීම.",
                        "நுனியிலிருந்து தொடங்கும் இலைகளின் மஞ்சள் முதல் ஆரஞ்சு நிற மாற்றம், வளர்ச்சி குன்றுதல், "
                                + "கிளைத்தல் குறைதல் மற்றும் பூக்கும் காலம் தாமதமாதல்."),
                new Text(
                        "There is no cure for infected plants — remove and destroy them. Control the leafhopper "
                                + "vector and follow local Department of Agriculture recommendations.",
                        "ආසාදිත ශාක සඳහා ප්‍රතිකාරයක් නොමැත — ඒවා ඉවත් කර විනාශ කරන්න. පත්‍ර පනුවන් පාලනය කර "
                                + "ප්‍රාදේශීය කෘෂිකර්ම දෙපාර්තමේන්තුවේ නිර්දේශ අනුගමනය කරන්න.",
                        "பாதிக்கப்பட்ட செடிகளுக்கு சிகிச்சை இல்லை — அவற்றை அகற்றி அழிக்கவும். இலைத்தத்துப்பூச்சி "
                                + "வாகனத்தைக் கட்டுப்படுத்தி உள்ளூர் வேளாண் திணைக்கள பரிந்துரைகளைப் பின்பற்றவும்."),
                new Text(
                        "Use resistant varieties, synchronise planting across the area, control leafhopper "
                                + "populations, and remove volunteer/ratoon rice that harbours the virus.",
                        "ප්‍රතිරෝධී ප්‍රභේද භාවිතා කරන්න, ප්‍රදේශය පුරා සිටුවීම සමමුහුර්ත කරන්න, පත්‍ර පනුවන් "
                                + "ගහන පාලනය කරන්න, සහ වෛරසය රඳවා ගන්නා ස්වයංව හටගත් / නැවත වැවුණු වී ශාක ඉවත් කරන්න.",
                        "எதிர்ப்புத் திறன் கொண்ட ரகங்களைப் பயன்படுத்தவும், பகுதி முழுவதும் நடவை ஒருங்கிணைக்கவும், "
                                + "இலைத்தத்துப்பூச்சி எண்ணிக்கையைக் கட்டுப்படுத்தவும், வைரஸை தாங்கும் தானாக முளைத்த/மறு "
                                + "அறுவடை நெல்லை அகற்றவும்.")));

        diseaseRepository.save(disease("healthy",
                "Healthy", "නිරෝගී", "ஆரோக்கியமான",
                null, Severity.NONE,
                new Text("No disease detected.",
                        "රෝගයක් හඳුනාගෙන නොමැත.",
                        "நோய் எதுவும் கண்டறியப்படவில்லை."),
                new Text("The leaf appears healthy with uniform green colour and no visible lesions or discolouration.",
                        "පත්‍රය සමාන කොළ පැහැයෙන් යුතුව, පෙනෙන තුවාල හෝ වර්ණ වෙනස්කම් නොමැතිව නිරෝගී ලෙස පෙනේ.",
                        "இலை சீரான பச்சை நிறத்துடன், தெரியும் புண்கள் அல்லது நிற மாற்றம் இல்லாமல் ஆரோக்கியமாகத் தெரிகிறது."),
                new Text("No treatment needed. Continue good crop management practices.",
                        "ප්‍රතිකාරයක් අවශ්‍ය නොවේ. හොඳ බෝග කළමනාකරණ පිළිවෙත් දිගටම කරගෙන යන්න.",
                        "சிகிச்சை தேவையில்லை. நல்ல பயிர் மேலாண்மை நடைமுறைகளைத் தொடரவும்."),
                new Text("Maintain balanced fertilisation, proper water management and regular monitoring to keep the crop healthy.",
                        "බෝගය නිරෝගීව තබා ගැනීමට සමතුලිත පොහොර යෙදීම, නිසි ජල කළමනාකරණය සහ නිතිපතා නිරීක්ෂණය පවත්වා ගන්න.",
                        "பயிரை ஆரோக்கியமாக வைத்திருக்க சமச்சீர் உரமிடுதல், சரியான நீர் மேலாண்மை மற்றும் தொடர்ச்சியான "
                                + "கண்காணிப்பை பராமரிக்கவும்.")));

        log.info("Seeded {} diseases into the knowledge base.", diseaseRepository.count());
    }

    private record Text(String en, String si, String ta) {}

    private Disease disease(String key, String en, String si, String ta, String sci, Severity severity,
                            Text cause, Text symptoms, Text treatment, Text prevention) {
        Disease d = new Disease();
        d.setDiseaseKey(key);
        d.setNameEn(en);
        d.setNameSi(si);
        d.setNameTa(ta);
        d.setScientificName(sci);
        d.setSeverity(severity);
        d.setCause(cause.en());
        d.setCauseSi(cause.si());
        d.setCauseTa(cause.ta());
        d.setSymptoms(symptoms.en());
        d.setSymptomsSi(symptoms.si());
        d.setSymptomsTa(symptoms.ta());
        d.setTreatment(treatment.en());
        d.setTreatmentSi(treatment.si());
        d.setTreatmentTa(treatment.ta());
        d.setPrevention(prevention.en());
        d.setPreventionSi(prevention.si());
        d.setPreventionTa(prevention.ta());
        return d;
    }
}
