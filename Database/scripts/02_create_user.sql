CREATE USER 'dev'@'%' IDENTIFIED BY 'ip23kp2mysql';
GRANT ALL PRIVILEGES ON `intergrate-kp-2`.* TO 'dev'@'%';
FLUSH PRIVILEGES;
