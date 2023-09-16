package de.mrjulsen.trafficcraft.block.properties;

import java.util.stream.IntStream;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.proxy.ClientProxy;
import de.mrjulsen.trafficcraft.util.Utils;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

public enum TrafficSignShape implements StringRepresentable {
    CIRCLE("circle", 0, new int[] {0,1,2,3,4,5,6,7,8,9,22,23,24,25,26,27,28,29,30,31,65536,65537,65538,65539,65540,65541,65542,65543,65544,65545,65558,65559,65560,65561,65562,65563,65564,65565,65566,65567,131072,131073,131074,131075,131076,131077,131098,131099,131100,131101,131102,131103,196608,196609,196610,196611,196612,196613,196634,196635,196636,196637,196638,196639,262144,262145,262146,262147,262172,262173,262174,262175,327680,327681,327682,327683,327708,327709,327710,327711,393216,393217,393246,393247,458752,458753,458782,458783,524288,524289,524318,524319,589824,589825,589854,589855,1441792,1441793,1441822,1441823,1507328,1507329,1507358,1507359,1572864,1572865,1572894,1572895,1638400,1638401,1638430,1638431,1703936,1703937,1703938,1703939,1703964,1703965,1703966,1703967,1769472,1769473,1769474,1769475,1769500,1769501,1769502,1769503,1835008,1835009,1835010,1835011,1835012,1835013,1835034,1835035,1835036,1835037,1835038,1835039,1900544,1900545,1900546,1900547,1900548,1900549,1900570,1900571,1900572,1900573,1900574,1900575,1966080,1966081,1966082,1966083,1966084,1966085,1966086,1966087,1966088,1966089,1966102,1966103,1966104,1966105,1966106,1966107,1966108,1966109,1966110,1966111,2031616,2031617,2031618,2031619,2031620,2031621,2031622,2031623,2031624,2031625,2031638,2031639,2031640,2031641,2031642,2031643,2031644,2031645,2031646,2031647}),
	SQUARE("square", 1, new int[] {}),
	DIAMOND("diamond", 2, new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,18,19,20,21,22,23,24,25,26,27,28,29,30,31,65536,65537,65538,65539,65540,65541,65542,65543,65544,65545,65546,65547,65548,65549,65554,65555,65556,65557,65558,65559,65560,65561,65562,65563,65564,65565,65566,65567,131072,131073,131074,131075,131076,131077,131078,131079,131080,131081,131082,131083,131092,131093,131094,131095,131096,131097,131098,131099,131100,131101,131102,131103,196608,196609,196610,196611,196612,196613,196614,196615,196616,196617,196618,196619,196628,196629,196630,196631,196632,196633,196634,196635,196636,196637,196638,196639,262144,262145,262146,262147,262148,262149,262150,262151,262152,262153,262166,262167,262168,262169,262170,262171,262172,262173,262174,262175,327680,327681,327682,327683,327684,327685,327686,327687,327688,327689,327702,327703,327704,327705,327706,327707,327708,327709,327710,327711,393216,393217,393218,393219,393220,393221,393222,393223,393240,393241,393242,393243,393244,393245,393246,393247,458752,458753,458754,458755,458756,458757,458758,458759,458776,458777,458778,458779,458780,458781,458782,458783,524288,524289,524290,524291,524292,524293,524314,524315,524316,524317,524318,524319,589824,589825,589826,589827,589828,589829,589850,589851,589852,589853,589854,589855,655360,655361,655362,655363,655388,655389,655390,655391,720896,720897,720898,720899,720924,720925,720926,720927,786432,786433,786462,786463,851968,851969,851998,851999,1179648,1179649,1179678,1179679,1245184,1245185,1245214,1245215,1310720,1310721,1310722,1310723,1310748,1310749,1310750,1310751,1376256,1376257,1376258,1376259,1376284,1376285,1376286,1376287,1441792,1441793,1441794,1441795,1441796,1441797,1441818,1441819,1441820,1441821,1441822,1441823,1507328,1507329,1507330,1507331,1507332,1507333,1507354,1507355,1507356,1507357,1507358,1507359,1572864,1572865,1572866,1572867,1572868,1572869,1572870,1572871,1572888,1572889,1572890,1572891,1572892,1572893,1572894,1572895,1638400,1638401,1638402,1638403,1638404,1638405,1638406,1638407,1638424,1638425,1638426,1638427,1638428,1638429,1638430,1638431,1703936,1703937,1703938,1703939,1703940,1703941,1703942,1703943,1703944,1703945,1703958,1703959,1703960,1703961,1703962,1703963,1703964,1703965,1703966,1703967,1769472,1769473,1769474,1769475,1769476,1769477,1769478,1769479,1769480,1769481,1769494,1769495,1769496,1769497,1769498,1769499,1769500,1769501,1769502,1769503,1835008,1835009,1835010,1835011,1835012,1835013,1835014,1835015,1835016,1835017,1835018,1835019,1835028,1835029,1835030,1835031,1835032,1835033,1835034,1835035,1835036,1835037,1835038,1835039,1900544,1900545,1900546,1900547,1900548,1900549,1900550,1900551,1900552,1900553,1900554,1900555,1900564,1900565,1900566,1900567,1900568,1900569,1900570,1900571,1900572,1900573,1900574,1900575,1966080,1966081,1966082,1966083,1966084,1966085,1966086,1966087,1966088,1966089,1966090,1966091,1966092,1966093,1966098,1966099,1966100,1966101,1966102,1966103,1966104,1966105,1966106,1966107,1966108,1966109,1966110,1966111,2031616,2031617,2031618,2031619,2031620,2031621,2031622,2031623,2031624,2031625,2031626,2031627,2031628,2031629,2031634,2031635,2031636,2031637,2031638,2031639,2031640,2031641,2031642,2031643,2031644,2031645,2031646,2031647}),
	TRIANGLE("triangle", 3, new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,30,31,65536,65537,65538,65539,65540,65541,65542,65543,65544,65545,65546,65547,65548,65549,65550,65551,65552,65553,65554,65555,65556,65557,65558,65559,65566,65567,131072,131073,131074,131075,131076,131077,131078,131079,131080,131081,131082,131083,131084,131085,131086,131087,131088,131089,131090,131091,196608,196609,196610,196611,196612,196613,196614,196615,196616,196617,196618,196619,196620,196621,196622,196623,196624,196625,196626,196627,262144,262145,262146,262147,262148,262149,262150,262151,262152,262153,262154,262155,262156,262157,262158,262159,327680,327681,327682,327683,327684,327685,327686,327687,327688,327689,327690,327691,327692,327693,327694,327695,393216,393217,393218,393219,393220,393221,393222,393223,393224,393225,393226,393227,458752,458753,458754,458755,458756,458757,458758,458759,458760,458761,458762,458763,524288,524289,524290,524291,524292,524293,524294,524295,589824,589825,589826,589827,589828,589829,589830,589831,655360,655361,655362,655363,720896,720897,720898,720899,1310720,1310721,1310722,1310723,1376256,1376257,1376258,1376259,1441792,1441793,1441794,1441795,1441796,1441797,1441798,1441799,1507328,1507329,1507330,1507331,1507332,1507333,1507334,1507335,1572864,1572865,1572866,1572867,1572868,1572869,1572870,1572871,1572872,1572873,1572874,1572875,1638400,1638401,1638402,1638403,1638404,1638405,1638406,1638407,1638408,1638409,1638410,1638411,1703936,1703937,1703938,1703939,1703940,1703941,1703942,1703943,1703944,1703945,1703946,1703947,1703948,1703949,1703950,1703951,1769472,1769473,1769474,1769475,1769476,1769477,1769478,1769479,1769480,1769481,1769482,1769483,1769484,1769485,1769486,1769487,1835008,1835009,1835010,1835011,1835012,1835013,1835014,1835015,1835016,1835017,1835018,1835019,1835020,1835021,1835022,1835023,1835024,1835025,1835026,1835027,1900544,1900545,1900546,1900547,1900548,1900549,1900550,1900551,1900552,1900553,1900554,1900555,1900556,1900557,1900558,1900559,1900560,1900561,1900562,1900563,1966080,1966081,1966082,1966083,1966084,1966085,1966086,1966087,1966088,1966089,1966090,1966091,1966092,1966093,1966094,1966095,1966096,1966097,1966098,1966099,1966100,1966101,1966102,1966103,1966110,1966111,2031616,2031617,2031618,2031619,2031620,2031621,2031622,2031623,2031624,2031625,2031626,2031627,2031628,2031629,2031630,2031631,2031632,2031633,2031634,2031635,2031636,2031637,2031638,2031639,2031646,2031647}),
	TRIANGLE_DOWN("triangle_down", 4, new int[] {0,1,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,65536,65537,65544,65545,65546,65547,65548,65549,65550,65551,65552,65553,65554,65555,65556,65557,65558,65559,65560,65561,65562,65563,65564,65565,65566,65567,131084,131085,131086,131087,131088,131089,131090,131091,131092,131093,131094,131095,131096,131097,131098,131099,131100,131101,131102,131103,196620,196621,196622,196623,196624,196625,196626,196627,196628,196629,196630,196631,196632,196633,196634,196635,196636,196637,196638,196639,262160,262161,262162,262163,262164,262165,262166,262167,262168,262169,262170,262171,262172,262173,262174,262175,327696,327697,327698,327699,327700,327701,327702,327703,327704,327705,327706,327707,327708,327709,327710,327711,393236,393237,393238,393239,393240,393241,393242,393243,393244,393245,393246,393247,458772,458773,458774,458775,458776,458777,458778,458779,458780,458781,458782,458783,524312,524313,524314,524315,524316,524317,524318,524319,589848,589849,589850,589851,589852,589853,589854,589855,655388,655389,655390,655391,720924,720925,720926,720927,1310748,1310749,1310750,1310751,1376284,1376285,1376286,1376287,1441816,1441817,1441818,1441819,1441820,1441821,1441822,1441823,1507352,1507353,1507354,1507355,1507356,1507357,1507358,1507359,1572884,1572885,1572886,1572887,1572888,1572889,1572890,1572891,1572892,1572893,1572894,1572895,1638420,1638421,1638422,1638423,1638424,1638425,1638426,1638427,1638428,1638429,1638430,1638431,1703952,1703953,1703954,1703955,1703956,1703957,1703958,1703959,1703960,1703961,1703962,1703963,1703964,1703965,1703966,1703967,1769488,1769489,1769490,1769491,1769492,1769493,1769494,1769495,1769496,1769497,1769498,1769499,1769500,1769501,1769502,1769503,1835020,1835021,1835022,1835023,1835024,1835025,1835026,1835027,1835028,1835029,1835030,1835031,1835032,1835033,1835034,1835035,1835036,1835037,1835038,1835039,1900556,1900557,1900558,1900559,1900560,1900561,1900562,1900563,1900564,1900565,1900566,1900567,1900568,1900569,1900570,1900571,1900572,1900573,1900574,1900575,1966080,1966081,1966088,1966089,1966090,1966091,1966092,1966093,1966094,1966095,1966096,1966097,1966098,1966099,1966100,1966101,1966102,1966103,1966104,1966105,1966106,1966107,1966108,1966109,1966110,1966111,2031616,2031617,2031624,2031625,2031626,2031627,2031628,2031629,2031630,2031631,2031632,2031633,2031634,2031635,2031636,2031637,2031638,2031639,2031640,2031641,2031642,2031643,2031644,2031645,2031646,2031647}),
	RECTANGLE("rectangle", 5, new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,65536,65537,65538,65539,65540,65541,65542,65543,65544,65545,65546,65547,65548,65549,65550,65551,65552,65553,65554,65555,65556,65557,65558,65559,65560,65561,65562,65563,65564,65565,65566,65567,131072,131073,131074,131075,131076,131077,131078,131079,131080,131081,131082,131083,131084,131085,131086,131087,131088,131089,131090,131091,131092,131093,131094,131095,131096,131097,131098,131099,131100,131101,131102,131103,196608,196609,196610,196611,196612,196613,196614,196615,196616,196617,196618,196619,196620,196621,196622,196623,196624,196625,196626,196627,196628,196629,196630,196631,196632,196633,196634,196635,196636,196637,196638,196639,1835008,1835009,1835010,1835011,1835012,1835013,1835014,1835015,1835016,1835017,1835018,1835019,1835020,1835021,1835022,1835023,1835024,1835025,1835026,1835027,1835028,1835029,1835030,1835031,1835032,1835033,1835034,1835035,1835036,1835037,1835038,1835039,1900544,1900545,1900546,1900547,1900548,1900549,1900550,1900551,1900552,1900553,1900554,1900555,1900556,1900557,1900558,1900559,1900560,1900561,1900562,1900563,1900564,1900565,1900566,1900567,1900568,1900569,1900570,1900571,1900572,1900573,1900574,1900575,1966080,1966081,1966082,1966083,1966084,1966085,1966086,1966087,1966088,1966089,1966090,1966091,1966092,1966093,1966094,1966095,1966096,1966097,1966098,1966099,1966100,1966101,1966102,1966103,1966104,1966105,1966106,1966107,1966108,1966109,1966110,1966111,2031616,2031617,2031618,2031619,2031620,2031621,2031622,2031623,2031624,2031625,2031626,2031627,2031628,2031629,2031630,2031631,2031632,2031633,2031634,2031635,2031636,2031637,2031638,2031639,2031640,2031641,2031642,2031643,2031644,2031645,2031646,2031647}),	
	RECTANGLE_SMALL("rectangle_small", 6, new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,65536,65537,65538,65539,65540,65541,65542,65543,65544,65545,65546,65547,65548,65549,65550,65551,65552,65553,65554,65555,65556,65557,65558,65559,65560,65561,65562,65563,65564,65565,65566,65567,131072,131073,131074,131075,131076,131077,131078,131079,131080,131081,131082,131083,131084,131085,131086,131087,131088,131089,131090,131091,131092,131093,131094,131095,131096,131097,131098,131099,131100,131101,131102,131103,196608,196609,196610,196611,196612,196613,196614,196615,196616,196617,196618,196619,196620,196621,196622,196623,196624,196625,196626,196627,196628,196629,196630,196631,196632,196633,196634,196635,196636,196637,196638,196639,262144,262145,262146,262147,262148,262149,262150,262151,262152,262153,262154,262155,262156,262157,262158,262159,262160,262161,262162,262163,262164,262165,262166,262167,262168,262169,262170,262171,262172,262173,262174,262175,327680,327681,327682,327683,327684,327685,327686,327687,327688,327689,327690,327691,327692,327693,327694,327695,327696,327697,327698,327699,327700,327701,327702,327703,327704,327705,327706,327707,327708,327709,327710,327711,393216,393217,393218,393219,393220,393221,393222,393223,393224,393225,393226,393227,393228,393229,393230,393231,393232,393233,393234,393235,393236,393237,393238,393239,393240,393241,393242,393243,393244,393245,393246,393247,458752,458753,458754,458755,458756,458757,458758,458759,458760,458761,458762,458763,458764,458765,458766,458767,458768,458769,458770,458771,458772,458773,458774,458775,458776,458777,458778,458779,458780,458781,458782,458783,1572864,1572865,1572866,1572867,1572868,1572869,1572870,1572871,1572872,1572873,1572874,1572875,1572876,1572877,1572878,1572879,1572880,1572881,1572882,1572883,1572884,1572885,1572886,1572887,1572888,1572889,1572890,1572891,1572892,1572893,1572894,1572895,1638400,1638401,1638402,1638403,1638404,1638405,1638406,1638407,1638408,1638409,1638410,1638411,1638412,1638413,1638414,1638415,1638416,1638417,1638418,1638419,1638420,1638421,1638422,1638423,1638424,1638425,1638426,1638427,1638428,1638429,1638430,1638431,1703936,1703937,1703938,1703939,1703940,1703941,1703942,1703943,1703944,1703945,1703946,1703947,1703948,1703949,1703950,1703951,1703952,1703953,1703954,1703955,1703956,1703957,1703958,1703959,1703960,1703961,1703962,1703963,1703964,1703965,1703966,1703967,1769472,1769473,1769474,1769475,1769476,1769477,1769478,1769479,1769480,1769481,1769482,1769483,1769484,1769485,1769486,1769487,1769488,1769489,1769490,1769491,1769492,1769493,1769494,1769495,1769496,1769497,1769498,1769499,1769500,1769501,1769502,1769503,1835008,1835009,1835010,1835011,1835012,1835013,1835014,1835015,1835016,1835017,1835018,1835019,1835020,1835021,1835022,1835023,1835024,1835025,1835026,1835027,1835028,1835029,1835030,1835031,1835032,1835033,1835034,1835035,1835036,1835037,1835038,1835039,1900544,1900545,1900546,1900547,1900548,1900549,1900550,1900551,1900552,1900553,1900554,1900555,1900556,1900557,1900558,1900559,1900560,1900561,1900562,1900563,1900564,1900565,1900566,1900567,1900568,1900569,1900570,1900571,1900572,1900573,1900574,1900575,1966080,1966081,1966082,1966083,1966084,1966085,1966086,1966087,1966088,1966089,1966090,1966091,1966092,1966093,1966094,1966095,1966096,1966097,1966098,1966099,1966100,1966101,1966102,1966103,1966104,1966105,1966106,1966107,1966108,1966109,1966110,1966111,2031616,2031617,2031618,2031619,2031620,2031621,2031622,2031623,2031624,2031625,2031626,2031627,2031628,2031629,2031630,2031631,2031632,2031633,2031634,2031635,2031636,2031637,2031638,2031639,2031640,2031641,2031642,2031643,2031644,2031645,2031646,2031647}),	
	RECTANGLE_HORIZONTAL("rectangle_horizontal", 7, new int[] {24,25,26,27,28,29,30,31,65560,65561,65562,65563,65564,65565,65566,65567,131096,131097,131098,131099,131100,131101,131102,131103,196632,196633,196634,196635,196636,196637,196638,196639,262168,262169,262170,262171,262172,262173,262174,262175,327704,327705,327706,327707,327708,327709,327710,327711,393240,393241,393242,393243,393244,393245,393246,393247,458776,458777,458778,458779,458780,458781,458782,458783,524312,524313,524314,524315,524316,524317,524318,524319,589848,589849,589850,589851,589852,589853,589854,589855,655384,655385,655386,655387,655388,655389,655390,655391,720920,720921,720922,720923,720924,720925,720926,720927,786456,786457,786458,786459,786460,786461,786462,786463,851992,851993,851994,851995,851996,851997,851998,851999,917528,917529,917530,917531,917532,917533,917534,917535,983064,983065,983066,983067,983068,983069,983070,983071,1048600,1048601,1048602,1048603,1048604,1048605,1048606,1048607,1114136,1114137,1114138,1114139,1114140,1114141,1114142,1114143,1179672,1179673,1179674,1179675,1179676,1179677,1179678,1179679,1245208,1245209,1245210,1245211,1245212,1245213,1245214,1245215,1310744,1310745,1310746,1310747,1310748,1310749,1310750,1310751,1376280,1376281,1376282,1376283,1376284,1376285,1376286,1376287,1441816,1441817,1441818,1441819,1441820,1441821,1441822,1441823,1507352,1507353,1507354,1507355,1507356,1507357,1507358,1507359,1572888,1572889,1572890,1572891,1572892,1572893,1572894,1572895,1638424,1638425,1638426,1638427,1638428,1638429,1638430,1638431,1703960,1703961,1703962,1703963,1703964,1703965,1703966,1703967,1769496,1769497,1769498,1769499,1769500,1769501,1769502,1769503,1835032,1835033,1835034,1835035,1835036,1835037,1835038,1835039,1900568,1900569,1900570,1900571,1900572,1900573,1900574,1900575,1966104,1966105,1966106,1966107,1966108,1966109,1966110,1966111,2031640,2031641,2031642,2031643,2031644,2031645,2031646,2031647}),	
	SMALL_UPPER("small_upper", 8, new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,65536,65537,65538,65539,65540,65541,65542,65543,65544,65545,65546,65547,65548,65549,65550,65551,65552,65553,65554,65555,65556,65557,65558,65559,65560,65561,65562,65563,65564,65565,65566,65567,131088,131089,131090,131091,131092,131093,131094,131095,131096,131097,131098,131099,131100,131101,131102,131103,196624,196625,196626,196627,196628,196629,196630,196631,196632,196633,196634,196635,196636,196637,196638,196639,262160,262161,262162,262163,262164,262165,262166,262167,262168,262169,262170,262171,262172,262173,262174,262175,327696,327697,327698,327699,327700,327701,327702,327703,327704,327705,327706,327707,327708,327709,327710,327711,393232,393233,393234,393235,393236,393237,393238,393239,393240,393241,393242,393243,393244,393245,393246,393247,458768,458769,458770,458771,458772,458773,458774,458775,458776,458777,458778,458779,458780,458781,458782,458783,524304,524305,524306,524307,524308,524309,524310,524311,524312,524313,524314,524315,524316,524317,524318,524319,589840,589841,589842,589843,589844,589845,589846,589847,589848,589849,589850,589851,589852,589853,589854,589855,655376,655377,655378,655379,655380,655381,655382,655383,655384,655385,655386,655387,655388,655389,655390,655391,720912,720913,720914,720915,720916,720917,720918,720919,720920,720921,720922,720923,720924,720925,720926,720927,786448,786449,786450,786451,786452,786453,786454,786455,786456,786457,786458,786459,786460,786461,786462,786463,851984,851985,851986,851987,851988,851989,851990,851991,851992,851993,851994,851995,851996,851997,851998,851999,917520,917521,917522,917523,917524,917525,917526,917527,917528,917529,917530,917531,917532,917533,917534,917535,983056,983057,983058,983059,983060,983061,983062,983063,983064,983065,983066,983067,983068,983069,983070,983071,1048592,1048593,1048594,1048595,1048596,1048597,1048598,1048599,1048600,1048601,1048602,1048603,1048604,1048605,1048606,1048607,1114128,1114129,1114130,1114131,1114132,1114133,1114134,1114135,1114136,1114137,1114138,1114139,1114140,1114141,1114142,1114143,1179664,1179665,1179666,1179667,1179668,1179669,1179670,1179671,1179672,1179673,1179674,1179675,1179676,1179677,1179678,1179679,1245200,1245201,1245202,1245203,1245204,1245205,1245206,1245207,1245208,1245209,1245210,1245211,1245212,1245213,1245214,1245215,1310736,1310737,1310738,1310739,1310740,1310741,1310742,1310743,1310744,1310745,1310746,1310747,1310748,1310749,1310750,1310751,1376272,1376273,1376274,1376275,1376276,1376277,1376278,1376279,1376280,1376281,1376282,1376283,1376284,1376285,1376286,1376287,1441808,1441809,1441810,1441811,1441812,1441813,1441814,1441815,1441816,1441817,1441818,1441819,1441820,1441821,1441822,1441823,1507344,1507345,1507346,1507347,1507348,1507349,1507350,1507351,1507352,1507353,1507354,1507355,1507356,1507357,1507358,1507359,1572880,1572881,1572882,1572883,1572884,1572885,1572886,1572887,1572888,1572889,1572890,1572891,1572892,1572893,1572894,1572895,1638416,1638417,1638418,1638419,1638420,1638421,1638422,1638423,1638424,1638425,1638426,1638427,1638428,1638429,1638430,1638431,1703952,1703953,1703954,1703955,1703956,1703957,1703958,1703959,1703960,1703961,1703962,1703963,1703964,1703965,1703966,1703967,1769488,1769489,1769490,1769491,1769492,1769493,1769494,1769495,1769496,1769497,1769498,1769499,1769500,1769501,1769502,1769503,1835024,1835025,1835026,1835027,1835028,1835029,1835030,1835031,1835032,1835033,1835034,1835035,1835036,1835037,1835038,1835039,1900560,1900561,1900562,1900563,1900564,1900565,1900566,1900567,1900568,1900569,1900570,1900571,1900572,1900573,1900574,1900575,1966080,1966081,1966082,1966083,1966084,1966085,1966086,1966087,1966088,1966089,1966090,1966091,1966092,1966093,1966094,1966095,1966096,1966097,1966098,1966099,1966100,1966101,1966102,1966103,1966104,1966105,1966106,1966107,1966108,1966109,1966110,1966111,2031616,2031617,2031618,2031619,2031620,2031621,2031622,2031623,2031624,2031625,2031626,2031627,2031628,2031629,2031630,2031631,2031632,2031633,2031634,2031635,2031636,2031637,2031638,2031639,2031640,2031641,2031642,2031643,2031644,2031645,2031646,2031647}),	
	SMALL_LOWER("small_lower", 9, new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,65536,65537,65538,65539,65540,65541,65542,65543,65544,65545,65546,65547,65548,65549,65550,65551,65552,65553,65554,65555,65556,65557,65558,65559,65560,65561,65562,65563,65564,65565,65566,65567,131072,131073,131074,131075,131076,131077,131078,131079,131080,131081,131082,131083,131084,131085,131086,131087,196608,196609,196610,196611,196612,196613,196614,196615,196616,196617,196618,196619,196620,196621,196622,196623,262144,262145,262146,262147,262148,262149,262150,262151,262152,262153,262154,262155,262156,262157,262158,262159,327680,327681,327682,327683,327684,327685,327686,327687,327688,327689,327690,327691,327692,327693,327694,327695,393216,393217,393218,393219,393220,393221,393222,393223,393224,393225,393226,393227,393228,393229,393230,393231,458752,458753,458754,458755,458756,458757,458758,458759,458760,458761,458762,458763,458764,458765,458766,458767,524288,524289,524290,524291,524292,524293,524294,524295,524296,524297,524298,524299,524300,524301,524302,524303,589824,589825,589826,589827,589828,589829,589830,589831,589832,589833,589834,589835,589836,589837,589838,589839,655360,655361,655362,655363,655364,655365,655366,655367,655368,655369,655370,655371,655372,655373,655374,655375,720896,720897,720898,720899,720900,720901,720902,720903,720904,720905,720906,720907,720908,720909,720910,720911,786432,786433,786434,786435,786436,786437,786438,786439,786440,786441,786442,786443,786444,786445,786446,786447,851968,851969,851970,851971,851972,851973,851974,851975,851976,851977,851978,851979,851980,851981,851982,851983,917504,917505,917506,917507,917508,917509,917510,917511,917512,917513,917514,917515,917516,917517,917518,917519,983040,983041,983042,983043,983044,983045,983046,983047,983048,983049,983050,983051,983052,983053,983054,983055,1048576,1048577,1048578,1048579,1048580,1048581,1048582,1048583,1048584,1048585,1048586,1048587,1048588,1048589,1048590,1048591,1114112,1114113,1114114,1114115,1114116,1114117,1114118,1114119,1114120,1114121,1114122,1114123,1114124,1114125,1114126,1114127,1179648,1179649,1179650,1179651,1179652,1179653,1179654,1179655,1179656,1179657,1179658,1179659,1179660,1179661,1179662,1179663,1245184,1245185,1245186,1245187,1245188,1245189,1245190,1245191,1245192,1245193,1245194,1245195,1245196,1245197,1245198,1245199,1310720,1310721,1310722,1310723,1310724,1310725,1310726,1310727,1310728,1310729,1310730,1310731,1310732,1310733,1310734,1310735,1376256,1376257,1376258,1376259,1376260,1376261,1376262,1376263,1376264,1376265,1376266,1376267,1376268,1376269,1376270,1376271,1441792,1441793,1441794,1441795,1441796,1441797,1441798,1441799,1441800,1441801,1441802,1441803,1441804,1441805,1441806,1441807,1507328,1507329,1507330,1507331,1507332,1507333,1507334,1507335,1507336,1507337,1507338,1507339,1507340,1507341,1507342,1507343,1572864,1572865,1572866,1572867,1572868,1572869,1572870,1572871,1572872,1572873,1572874,1572875,1572876,1572877,1572878,1572879,1638400,1638401,1638402,1638403,1638404,1638405,1638406,1638407,1638408,1638409,1638410,1638411,1638412,1638413,1638414,1638415,1703936,1703937,1703938,1703939,1703940,1703941,1703942,1703943,1703944,1703945,1703946,1703947,1703948,1703949,1703950,1703951,1769472,1769473,1769474,1769475,1769476,1769477,1769478,1769479,1769480,1769481,1769482,1769483,1769484,1769485,1769486,1769487,1835008,1835009,1835010,1835011,1835012,1835013,1835014,1835015,1835016,1835017,1835018,1835019,1835020,1835021,1835022,1835023,1900544,1900545,1900546,1900547,1900548,1900549,1900550,1900551,1900552,1900553,1900554,1900555,1900556,1900557,1900558,1900559,1966080,1966081,1966082,1966083,1966084,1966085,1966086,1966087,1966088,1966089,1966090,1966091,1966092,1966093,1966094,1966095,1966096,1966097,1966098,1966099,1966100,1966101,1966102,1966103,1966104,1966105,1966106,1966107,1966108,1966109,1966110,1966111,2031616,2031617,2031618,2031619,2031620,2031621,2031622,2031623,2031624,2031625,2031626,2031627,2031628,2031629,2031630,2031631,2031632,2031633,2031634,2031635,2031636,2031637,2031638,2031639,2031640,2031641,2031642,2031643,2031644,2031645,2031646,2031647}),	
	OCTAGON("octagon", 10, new int[] {0,1,2,3,4,5,6,7,24,25,26,27,28,29,30,31,65536,65537,65538,65539,65540,65541,65542,65543,65560,65561,65562,65563,65564,65565,65566,65567,131072,131073,131074,131075,131076,131077,131098,131099,131100,131101,131102,131103,196608,196609,196610,196611,196612,196613,196634,196635,196636,196637,196638,196639,262144,262145,262146,262147,262172,262173,262174,262175,327680,327681,327682,327683,327708,327709,327710,327711,393216,393217,393246,393247,458752,458753,458782,458783,1572864,1572865,1572894,1572895,1638400,1638401,1638430,1638431,1703936,1703937,1703938,1703939,1703964,1703965,1703966,1703967,1769472,1769473,1769474,1769475,1769500,1769501,1769502,1769503,1835008,1835009,1835010,1835011,1835012,1835013,1835034,1835035,1835036,1835037,1835038,1835039,1900544,1900545,1900546,1900547,1900548,1900549,1900570,1900571,1900572,1900573,1900574,1900575,1966080,1966081,1966082,1966083,1966084,1966085,1966086,1966087,1966104,1966105,1966106,1966107,1966108,1966109,1966110,1966111,2031616,2031617,2031618,2031619,2031620,2031621,2031622,2031623,2031640,2031641,2031642,2031643,2031644,2031645,2031646,2031647}),	
	MISC("misc", 11, new int[] {});
	
	private String shape;
	private int index;
	private int[] invalidPixels;

	public static final int MAX_WIDTH = 32;
	public static final int MAX_HEIGHT = 32;
	
	private TrafficSignShape(String shape, int index, int[] invalidPixels) {
		this.shape = shape;
		this.index = index;
		this.invalidPixels = invalidPixels;
	}
	
	public String getShape() {
		return this.shape;
	}

	public int getIndex() {
		return this.index;
	}

	public String getTranslationKey() {
		return String.format("gui.trafficcraft.signshape.%s", shape);
	}

	public boolean isPixelValid(int x, int y) {
		return !IntStream.of(invalidPixels).anyMatch(a -> a == Utils.coordsToInt((byte)Mth.clamp(x, 0, MAX_WIDTH), (byte)Mth.clamp(y, 0, MAX_HEIGHT)));
	}

	public static TrafficSignShape getShapeByIndex(int index) {
		for (TrafficSignShape shape : TrafficSignShape.values()) {
			if (shape.getIndex() == index) {
				return shape;
			}
		}
		return TrafficSignShape.CIRCLE;
	}

	@OnlyIn(Dist.CLIENT)
	public DynamicTexture getShapeTexture() {
		return ClientProxy.SHAPE_TEXTURES[this.getIndex()];
	}

	public int getShapeTextureId() {
		return this.getShapeTexture().getId();
	}

	public final ResourceLocation getIconResourceLocation() {
		return new ResourceLocation(ModMain.MOD_ID, String.format("textures/block/sign/icons/%s.png", this.getShape()));
	}

	public VoxelShape getVoxelShape(Direction direction) {
		final VoxelShape SHAPE_COMMON = Block.box(7, 0, 7, 9, 16, 9);
		// S, W, N, E
		VoxelShape[] shapes;
		switch (this) {			
			case RECTANGLE:
				shapes = new VoxelShape[] {
					
					Shapes.or(SHAPE_COMMON, Block.box(2, 0, 9, 14, 16, 9.5)),
					Shapes.or(SHAPE_COMMON, Block.box(6.5, 0, 2, 7, 16, 14)),
					Shapes.or(SHAPE_COMMON, Block.box(2, 0, 6.5, 14, 16, 7)),
					Shapes.or(SHAPE_COMMON, Block.box(9, 0, 2, 9.5, 16, 14))
				};
				return shapes[direction.get2DDataValue()];
			case RECTANGLE_HORIZONTAL:
				shapes = new VoxelShape[] {					
					Shapes.or(SHAPE_COMMON, Block.box(0, 4, 9, 16, 16, 9.5)),
					Shapes.or(SHAPE_COMMON, Block.box(6.5, 4, 0, 7, 16, 16)),
					Shapes.or(SHAPE_COMMON, Block.box(0, 4, 6.5, 16, 16, 7)),
					Shapes.or(SHAPE_COMMON, Block.box(9, 4, 0, 9.5, 16, 16))
				};
				return shapes[direction.get2DDataValue()];
			case RECTANGLE_SMALL:
				shapes = new VoxelShape[] {
					
					Shapes.or(SHAPE_COMMON, Block.box(4, 0, 9, 12, 16, 9.5)),
					Shapes.or(SHAPE_COMMON, Block.box(6.5, 0, 4, 7, 16, 12)),
					Shapes.or(SHAPE_COMMON, Block.box(4, 0, 6.5, 12, 16, 7)),
					Shapes.or(SHAPE_COMMON, Block.box(9, 0, 4, 9.5, 16, 12))
				};
				return shapes[direction.get2DDataValue()];
			case SMALL_LOWER:
				shapes = new VoxelShape[] {
					
					Shapes.or(Block.box(1, 0, 9, 15, 8, 9.5), Block.box(7, 0, 7, 9, 8, 9)),
					Shapes.or(Block.box(6.5, 0, 1, 7, 8, 15), Block.box(7, 0, 7, 9, 8, 9)),
					Shapes.or(Block.box(1, 0, 6.5, 15, 8, 7), Block.box(7, 0, 7, 9, 8, 9)),
					Shapes.or(Block.box(9, 0, 1, 9.5, 8, 15), Block.box(7, 0, 7, 9, 8, 9))
				};
				return shapes[direction.get2DDataValue()];
			case SMALL_UPPER:
				shapes = new VoxelShape[] {
					
					Shapes.or(SHAPE_COMMON, Block.box(1, 8, 9, 15, 16, 9.5)),
					Shapes.or(SHAPE_COMMON, Block.box(6.5, 8, 1, 7, 16, 15)),
					Shapes.or(SHAPE_COMMON, Block.box(1, 8, 6.5, 15, 16, 7)),
					Shapes.or(SHAPE_COMMON, Block.box(9, 8, 1, 9.5, 16, 15))
				};
				return shapes[direction.get2DDataValue()];
			default:
				shapes = new VoxelShape[] {
					Shapes.or(SHAPE_COMMON, Block.box(0, 0, 9, 16, 16, 9.5D)),
					Shapes.or(SHAPE_COMMON, Block.box(6.5D, 0, 0, 7, 16, 16)),
					Shapes.or(SHAPE_COMMON, Block.box(0, 0, 6.5D, 16, 16, 7)),
					Shapes.or(SHAPE_COMMON, Block.box(9, 0, 0, 9.5D, 16, 16))
				};
				return shapes[direction.get2DDataValue()];
		}
	}

	public int[] getInvalidPixels() {
		return invalidPixels;
	}

    @Override
    public String getSerializedName() {
        return shape;
    }
}
