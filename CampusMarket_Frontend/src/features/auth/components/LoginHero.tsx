import { Logo } from '@/components/Logo/Logo';
import styles from './LoginHero.module.css';

const VALUE_PROPS = [
  {
    icon: '✓',
    title: 'Verified students only',
    description: 'Every account confirmed with a college email.',
  },
  {
    icon: '₹',
    title: 'Fair, campus prices',
    description: 'Books, cycles, laptops — priced by students, for students.',
  },
];

export function LoginHero() {
  return (
    <div>
      <Logo size="md" light />
      <h1 className={styles.title}>Your campus.<br />Your marketplace.</h1>
      <p className={styles.subtitle}>
        Buy and sell secondhand items with verified students from your own college.
      </p>
      {VALUE_PROPS.map((prop) => (
        <div className={styles.valueProp} key={prop.title}>
          <div className={styles.stampDot}>{prop.icon}</div>
          <div>
            <div className={styles.valuePropTitle}><b>{prop.title}</b></div>
            <div className={styles.valuePropDesc}>{prop.description}</div>
          </div>
        </div>
      ))}
    </div>
  );
}