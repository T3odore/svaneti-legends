package com.example.data

data class HistoryFact(
    val id: String,
    val titleGe: String,
    val titleEn: String,
    val eraGe: String,
    val eraEn: String,
    val textGe: String,
    val textEn: String,
    val unlockRequirementGe: String,
    val unlockRequirementEn: String,
    val iconSymbol: String, // Emoji or visual identifier
    val sourceUrl: String // Wikipedia or other resource link
)

object HistoryFactsProvider {
    val facts = listOf(
        HistoryFact(
            id = "intro",
            titleGe = "საქართველო • კოლხეთი და იბერია",
            titleEn = "Georgia • Colchis & Iberia",
            eraGe = "ანტიკური ხანა",
            eraEn = "Ancient Era & Roots",
            textGe = "კავკასიის გულში მდებარე საქართველო, ერთ-ერთი უძველესი ქვეყანაა მსოფლიოში. ბერძნულ მითოლოგიაში კოლხეთი მიიჩნეოდა ოქროს საწმისის ქვეყნად, სადაც არგონავტებმა იმოგზაურეს. აღმოსავლეთით მდებარე იბერიის სამეფომ კი საფუძველი ჩაუყარა ერთიან ქართულ სახელმწიფოებრიობას.",
            textEn = "Nestled in the heart of the Caucasus, Georgia is one of the world's oldest nations. In Greek mythology, western Georgia was Colchis, the magical land of the Golden Fleece sought by Argonauts. In the east, the Kingdom of Iberia laid the foundation for unified Georgian statehood.",
            unlockRequirementGe = "დაწყებითი ცოდნა (თავისუფალია)",
            unlockRequirementEn = "Unlocked at start",
            iconSymbol = "🛡️",
            sourceUrl = "https://en.wikipedia.org/wiki/Georgia_(country)"
        ),
        HistoryFact(
            id = "kvevri",
            titleGe = "ღვინის აკვანი და ქვევრი",
            titleEn = "Cradle of Wine & Kvevri",
            eraGe = "ძვ. წ. VI ათასწლეული",
            eraEn = "6000 BC",
            textGe = "არქეოლოგიურმა აღმოჩენებმა დაადასტურა, რომ საქართველო ღვინის სამშობლოა - აქ 8000 წელზე მეტია უწყვეტად მზადდება ღვინო უნიკალური თიხის ჭურჭელში, რომელსაც ქვევრი ჰქვია. ეს ტრადიციული მეთოდი იუნესკოს არამატერიალური კულტურული მემკვიდრეობის ძეგლია.",
            textEn = "Archaeological findings confirm Georgia as the birthplace of wine, boasting an unbroken 8,000-year winemaking history. Wine is fermented in ancient, egg-shaped clay vessels called Kvevri, buried underground. This unique method is listed as a UNESCO Intangible Cultural Heritage.",
            unlockRequirementGe = "მოაგროვეთ 200 ქულა ან იპოვეთ ქვევრი",
            unlockRequirementEn = "Reach 200 pts or collect a Kvevri",
            iconSymbol = "🏺",
            sourceUrl = "https://en.wikipedia.org/wiki/Kvevri"
        ),
        HistoryFact(
            id = "svan_towers",
            titleGe = "სვანური კოშკები",
            titleEn = "Svaneti Towers",
            eraGe = "VIII - XII საუკუნეები",
            eraEn = "8th - 12th Century",
            textGe = "სვანური კოშკი - უნიკალური თავდაცვითი ნაგებობაა, რომელიც იცავდა სვანეთის მოსახლეობას ზვავებისგან და მტრებისგან. ეს მონუმენტური ქვის კოშკები დღემდე დგას სვანეთის მთებში და მთლიანად უშგული იუნესკოს მსოფლიო მემკვიდრეობის ძეგლს წარმოადგენს.",
            textEn = "Svan towers are monumental stone defense towers built to protect highlanders from avalanches and invaders. Towering over ancient villages like Ushguli, these fortress-homes are iconic architectural masterpieces of Svaneti and are recognized by UNESCO.",
            unlockRequirementGe = "მიაღწიეთ მე-2 დონეს თამაშში",
            unlockRequirementEn = "Reach Level 2 in the game",
            iconSymbol = "🏰",
            sourceUrl = "https://en.wikipedia.org/wiki/Svan_tower"
        ),
        HistoryFact(
            id = "david",
            titleGe = "დავით აღმაშენებელი და დიდგორი",
            titleEn = "David the Builder & Didgori",
            eraGe = "XI - XII საუკუნეები",
            eraEn = "1089 – 1125 AD",
            textGe = "მეფე დავით IV-მ გააერთიანა დაქუცმაცებული საქართველო და შექმნა უძლიერესი სახელმწიფო. 1121 წლის დიდგორის ბრძოლაში, რომელიც 'ძლევაი საკვირველის' სახელითაა ცნობილი, ქართველებმა დაამარცხეს კოალიციური მტრის მრავალრიცხოვანი არმია.",
            textEn = "King David IV (The Builder) united fractured principalities and forged a medieval golden empire. In 1121, at the Battle of Didgori (known as the 'Miraculous Victory'), his outnumbered army decisively routed a massive coalition force, securing Georgia's sovereignty.",
            unlockRequirementGe = "იპოვეთ 1 ისტორიის გრაგნილი",
            unlockRequirementEn = "Collect 1 History Scroll in play",
            iconSymbol = "⚔️",
            sourceUrl = "https://en.wikipedia.org/wiki/David_IV_of_Georgia"
        ),
        HistoryFact(
            id = "tamar",
            titleGe = "თამარ მეფე • ოქროს ხანა",
            titleEn = "King Tamar & Golden Age",
            eraGe = "XII - XIII საუკუნეები",
            eraEn = "1184 – 1213 AD",
            textGe = "თამარი იყო პირველი ქალი მმართველი საქართველოს ისტორიაში, რომელსაც 'მეფეთ მეფე' უწოდეს. მისი მმართველობის ხანა ქართული კულტურის, მეცნიერების, არქიტექტურისა და ხელოვნების უმაღლესი აყვავების პერიოდია (ოქროს ხანა).",
            textEn = "Tamar became the first woman to rule Georgia in her own right, crowned with the title 'King of Kings'. Under her wise reign, medieval Georgia experienced its peak golden era of cultural renaissance, architectural grandeur, and scientific advancement.",
            unlockRequirementGe = "მოაგროვეთ 500 ქულა თამაშში",
            unlockRequirementEn = "Reach 500 points in one run",
            iconSymbol = "👑",
            sourceUrl = "https://en.wikipedia.org/wiki/Tamar_of_Georgia"
        ),
        HistoryFact(
            id = "vardzia",
            titleGe = "ვარძიის კლდის ქალაქი",
            titleEn = "Vardzia Cave City",
            eraGe = "XII საუკუნე",
            eraEn = "12th Century",
            textGe = "ვარძია - კლდეში ნაკვეთი გიგანტური სამონასტრო ქალაქია, რომელიც თამარ მეფის ბრძანებით გაფართოვდა. მასში 13 სართულზე განლაგებული 6000-მდე ოთახი, ეკლესიები, საიდუმლო გვირაბები და უნიკალური წყალსადენი სისტემა იყო ინტეგრირებული.",
            textEn = "Vardzia is a spectacular cave fortress-monastery carved sheer into the side of Erusheti mountain, constructed on instructions of King Tamar. It housed up to 6,000 rooms across 13 subterranean tiers, featuring secret escape tunnels, libraries, and advanced water systems.",
            unlockRequirementGe = "მოაგროვეთ 3 ისტორიის გრაგნილი",
            unlockRequirementEn = "Collect 3 History Scrolls overall",
            iconSymbol = "🧗",
            sourceUrl = "https://en.wikipedia.org/wiki/Vardzia"
        ),
        HistoryFact(
            id = "rustaveli",
            titleGe = "ვეფხისტყაოსანი",
            titleEn = "Knight in the Panther's Skin",
            eraGe = "XII საუკუნე",
            eraEn = "12th Century",
            textGe = "შოთა რუსთაველის მიერ თამარ მეფის ეპოქაში დაწერილი 'ვეფხისტყაოსანი' ქართული ლიტერატურის გვირგვინია. იგი მეგობრობის, თავდადების, სიყვარულისა და თავისუფლების ჰიმნია და მსოფლიო ლიტერატურის შედევრს წარმოადგენს.",
            textEn = "Written by Shota Rustaveli during the Golden Age, 'The Knight in the Panther's Skin' is Georgia's grand epic. Celebrating loyalty, friendship, courage, and gender equality, it is widely revered as a pinnacle masterpiece of world medieval literature.",
            unlockRequirementGe = "მოაგროვეთ 1000 ქულა ან იპოვეთ წიგნი",
            unlockRequirementEn = "Reach 1000 pts or find Epic Scroll",
            iconSymbol = "📖",
            sourceUrl = "https://en.wikipedia.org/wiki/The_Knight_in_the_Panther%27s_Skin"
        ),
        HistoryFact(
            id = "alphabet",
            titleGe = "ქართული ანბანი • სამი უნიკალური სისტემა",
            titleEn = "Georgian Alphabet • Three Unique Scripts",
            eraGe = "ანტიკური ხანიდან დღემდე",
            eraEn = "Antiquity to Present Day",
            textGe = "ქართული ანბანი მსოფლიოში ერთ-ერთი უნიკალურია და აქვს განვითარების სამი საფეხური: ასომთავრული, ნუსხური და მხედრული. სამივე სისტემა დღემდე ცოცხალია და იუნესკოს მიერ აღიარებულია, როგორც კაცობრიობის არამატერიალური კულტურული მემკვიდრეობა.",
            textEn = "The Georgian script is one of the world's few unique writing systems, with three evolutionary phases: Asomtavruli, Nuskhuri, and Mkhedruli. All three writing systems remain in use and are recognized by UNESCO as Intangible Cultural Heritage of Humanity.",
            unlockRequirementGe = "მიაღწიეთ მე-3 დონეს თამაშში",
            unlockRequirementEn = "Reach Level 3 in the game",
            iconSymbol = "✍️",
            sourceUrl = "https://en.wikipedia.org/wiki/Georgian_scripts"
        ),
        HistoryFact(
            id = "chakrulo",
            titleGe = "ჩაკრულო • ქართული მრავალხმიანობა კოსმოსში",
            titleEn = "Chakrulo • Georgian Polyphony in Space",
            eraGe = "უძველესი ფოლკლორი",
            eraEn = "Ancient Folklore / 1977 Space Voyage",
            textGe = "„ჩაკრულო“ არის ქართული ხალხური საგმირო სიმღერა. 1977 წელს, NASA-მ იგი გაგზავნა კოსმოსში ხომალდ „Voyager“-ით, როგორც კაცობრიობის კულტურის ერთ-ერთი უმნიშვნელოვანესი ნიმუში. ქართული ტრადიციული პოლიფონია აღიარებულია იუნესკოს მსოფლიო შედევრად.",
            textEn = "'Chakrulo' is an ancient Georgian polyphonic patriotic song. In 1977, NASA launched Voyager 1 and 2 with a Golden Record featuring Chakrulo as a supreme example of human cultural achievement. Georgian traditional polyphony is a UNESCO Masterpiece.",
            unlockRequirementGe = "მიაღწიეთ მე-4 დონეს თამაშში",
            unlockRequirementEn = "Reach Level 4 in the game",
            iconSymbol = "🚀",
            sourceUrl = "https://en.wikipedia.org/wiki/Chakrulo"
        ),
        HistoryFact(
            id = "amirani",
            titleGe = "ამირანის მითი • კავკასიელი პრომეთე",
            titleEn = "Amirani Myth • The Caucasian Prometheus",
            eraGe = "ბრინჯაოს ხანა",
            eraEn = "Bronze Age Mythology",
            textGe = "ქართულ მითოლოგიაში ამირანი არის ნახევარღმერთი გმირი, რომელმაც ხალხს ცეცხლი და ლითონის დამუშავება ასწავლა. ამის გამო იგი ღმერთმა კავკასიონის ქედზე, ხვამლის კლდეზე მიაჯაჭვა. ეს მითი ბერძნული პრომეთეს უძველესი წინამორბედია.",
            textEn = "Amirani is a legendary Georgian demigod and culture hero who taught humankind the secrets of fire and metallurgy. Punished by the gods, he was chained to Mount Khvamli in the Caucasus. This epic legend is the ancient precursor to the Greek myth of Prometheus.",
            unlockRequirementGe = "მიაღწიეთ მე-5 დონეს თამაშში",
            unlockRequirementEn = "Reach Level 5 in the game",
            iconSymbol = "⛓️",
            sourceUrl = "https://en.wikipedia.org/wiki/Amirani"
        )
    )
}
