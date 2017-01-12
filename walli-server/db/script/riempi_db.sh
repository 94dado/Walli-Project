cat ../db_scheme.sql ../db_data.sql | mysql -u Walli -p
rm ../../image/user/* ../../image/group/* ../../image/timestamp.json
echo Done!
