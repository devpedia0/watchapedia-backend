insert into user values (1, now(), now(), 1, 'KR', '', 'aaa1@bbb.ccc', 'N', 'N', 'N', 'N', 'testName', '1234'),
                        (2, now(), now(), 1, 'KR', '', 'aaa2@bbb.ccc', 'N', 'N', 'N', 'N', 'testName', '1234');

insert into comment values (100, 1, now(), now(), 'N', 'desc', 'N'),
                           (200, 1, now(), now(), 'N', 'desc', 'N'),
                           (300, 1, now(), now(), 'N', 'desc', 'N');

insert into interest values (100, 1, 1), (101, 1, 2), (102, 1, 3),
                            (200, 1, 1), (201, 1, 2), (202, 1, 3),
                            (300, 1, 1), (301, 1, 2), (302, 1, 3);

insert into score values (100, 1, 3.0), (200, 1, 4.0), (300, 1, 5.0);